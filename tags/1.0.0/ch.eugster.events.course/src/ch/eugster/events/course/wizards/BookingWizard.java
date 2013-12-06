package ch.eugster.events.course.wizards;

import java.io.File;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.documents.maps.BookingMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.queries.BookingQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class BookingWizard extends Wizard implements IBookingWizard
{

	private Booking booking;

	public BookingWizard(final Booking booking)
	{
		this.booking = booking;
	}

	@Override
	public boolean canFinish()
	{
		for (IWizardPage page : this.getPages())
		{
			if (!page.isPageComplete())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public Booking getBooking()
	{
		return this.booking;
	}

	@Override
	public boolean performFinish()
	{
		CourseWizardPage coursePage = (CourseWizardPage) this.getPage("courseWizardPage");
		if (coursePage != null)
		{
			coursePage.update(this.booking);
		}
		BookingWizardPage bookingPage = (BookingWizardPage) this.getPage("bookingWizardPage");
		if (bookingPage != null)
		{
			bookingPage.update(this.booking);
		}
		ParticipantWizardPage participantPage = (ParticipantWizardPage) this.getPage("participantWizardPage");
		if (participantPage != null)
		{
			participantPage.update(this.booking);
		}
		if (bookingPage != null)
		{
			if (bookingPage.printBookingConfirmation())
			{
				this.booking.setBookingConfirmationSentDate(GregorianCalendar.getInstance());
			}
			if (bookingPage.printInvitation())
			{
				this.booking.setInvitationSentDate(GregorianCalendar.getInstance());
			}
		}

		IStatus status = saveBooking();
		if (bookingPage != null)
		{
			if (status.getSeverity() == IStatus.ERROR)
			{
				MessageDialog dialog = new MessageDialog(this.getShell(), "Fehler", null,
						"Beim Speichern der Buchung ist ein Fehler aufgetreten.", MessageDialog.ERROR,
						new String[] { "OK" }, 0);
				dialog.open();
			}
			if (status.isOK() && bookingPage.printBookingConfirmation())
			{
				printBookingConfirmation(bookingPage);
			}
			if (status.isOK() && bookingPage.printInvitation())
			{
				printInvitation(bookingPage);
			}
		}
		return true;
	}

	@Override
	public boolean performCancel()
	{
		return this.resetBooking();
	}

	private IStatus printBookingConfirmation(final BookingWizardPage bookingPage)
	{
		IStatus status = Status.OK_STATUS;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class.getName(), null);
		tracker.open();
		try
		{
			Object service = tracker.getService();
			if (service instanceof DocumentBuilderService)
			{
				File template = new File(bookingPage.getBookingConfirmationTemplatePath());
				DataMap map = new BookingMap(this.booking);
				DocumentBuilderService builderService = (DocumentBuilderService) service;
				builderService.buildDocument(template, map);
			}
			else
			{
				status = new Status(
						IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						"Der Service für die Aufbereitung der Buchungsbestätigung ist nicht aktiv. Die Buchungsbestätigung kann nicht gedruckt werden.");
			}
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					"Beim Aufbereiten der Buchungsbestätigung ist ist ein Fehler aufgetreten.");
		}
		finally
		{
			tracker.close();
		}
		return status;
	}

	private IStatus printInvitation(final BookingWizardPage bookingPage)
	{
		IStatus status = Status.OK_STATUS;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class.getName(), null);
		tracker.open();
		try
		{
			Object service = tracker.getService();
			if (service instanceof DocumentBuilderService)
			{
				File template = new File(bookingPage.getInvitationTemplatePath());
				DataMap map = new BookingMap(this.booking);
				DocumentBuilderService builderService = (DocumentBuilderService) service;
				builderService.buildDocument(template, map);
			}
			else
			{
				status = new Status(
						IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						"Der Service für die Aufbereitung der Buchungsbestätigung ist nicht aktiv. Die Buchungsbestätigung kann nicht gedruckt werden.");
			}
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					"Beim Aufbereiten der Buchungsbestätigung ist ist ein Fehler aufgetreten.");
		}
		finally
		{
			tracker.close();
		}
		return status;
	}

	private IStatus saveBooking()
	{
		IStatus status = Status.OK_STATUS;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			Object service = tracker.getService();
			if (service instanceof ConnectionService)
			{
				ConnectionService connectionService = (ConnectionService) service;
				BookingQuery query = (BookingQuery) connectionService.getQuery(Booking.class);
				this.booking = query.merge(this.booking);
			}
			else
			{
				status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
						"Die Datenbankverbindung ist nicht aktiv, die Buchung kann nicht gespeichert werden.");
			}
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					"Beim Speichern der Buchung ist ein Fehler aufgetreten.", e);
		}
		finally
		{
			tracker.close();
		}
		return status;
	}

	private boolean resetBooking()
	{
		if (booking.getId() != null)
		{
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			try
			{
				Object service = tracker.getService();
				if (service instanceof ConnectionService)
				{
					ConnectionService connectionService = (ConnectionService) service;
					this.booking = (Booking) connectionService.refresh(this.booking);
				}
			}
			finally
			{
				tracker.close();
			}
		}
		return true;
	}
}
