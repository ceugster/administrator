package ch.eugster.events.course.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.getShell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
							DocumentBuilderService.class, null);
					tracker.open();
					try
					{
						monitor.beginTask("Buchungsbestätigung wird gedruckt...", 1);

						Object service = tracker.getService();
						if (service instanceof DocumentBuilderService)
						{
							File template = new File(bookingPage.getBookingConfirmationTemplatePath());
							DataMap map = new BookingMap(BookingWizard.this.booking);
							DocumentBuilderService builderService = (DocumentBuilderService) service;
							builderService.buildDocument(new SubProgressMonitor(monitor, 1), template, map);
						}
						else
						{
							MessageDialog
									.openError(
											getShell(),
											"Service nicht aktiv",
											"Der Service für die Aufbereitung der Buchungsbestätigung ist nicht aktiv. Die Buchungsbestätigung kann nicht gedruckt werden.");
						}
						monitor.worked(1);
					}
					catch (Exception e)
					{
						MessageDialog.openError(getShell(), "Fehler",
								"Beim Aufbereiten der Buchungsbestätigung ist ist ein Fehler aufgetreten.");
					}
					finally
					{
						tracker.close();
						monitor.done();
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			MessageDialog.openError(this.getShell(), "Fehler",
					"Bei der Verarbeitung ist ein Fehler aufgetreten\n(" + e.getLocalizedMessage() + ")");
		}
		catch (InterruptedException e)
		{
		}
		return status;
	}

	private IStatus printInvitation(final BookingWizardPage bookingPage)
	{
		IStatus status = Status.OK_STATUS;
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.getShell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
							DocumentBuilderService.class, null);
					tracker.open();
					try
					{
						monitor.beginTask("Kurseinladung wird gedruckt...", 1);

						Object service = tracker.getService();
						if (service instanceof DocumentBuilderService)
						{
							File template = new File(bookingPage.getInvitationTemplatePath());
							DataMap map = new BookingMap(BookingWizard.this.booking);
							DocumentBuilderService builderService = (DocumentBuilderService) service;
							builderService.buildDocument(new SubProgressMonitor(monitor, 1), template, map);
						}
						else
						{
							MessageDialog
									.openError(
											getShell(),
											"Service nicht aktiv",
											"Der Service für die Aufbereitung der Buchungsbestätigung ist nicht aktiv. Die Kurseinladung kann nicht gedruckt werden.");
						}
						monitor.worked(1);
					}
					catch (Exception e)
					{
						MessageDialog.openError(getShell(), "Fehler",
								"Beim Aufbereiten der Kurseinladung ist ist ein Fehler aufgetreten.");
					}
					finally
					{
						tracker.close();
						monitor.done();
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			MessageDialog.openError(this.getShell(), "Fehler",
					"Bei der Verarbeitung ist ein Fehler aufgetreten\n(" + e.getLocalizedMessage() + ")");
		}
		catch (InterruptedException e)
		{
		}
		return status;
	}

	private IStatus saveBooking()
	{
		IStatus status = Status.OK_STATUS;
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
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
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
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
