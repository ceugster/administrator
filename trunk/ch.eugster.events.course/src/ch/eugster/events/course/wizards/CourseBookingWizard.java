package ch.eugster.events.course.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.queries.BookingQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseBookingWizard extends Wizard
{

	private Booking booking;

	public CourseBookingWizard(final Booking booking)
	{
		this.booking = booking;
	}

	@Override
	public void addPages()
	{
		ParticipantWizardPage participantPage = new ParticipantWizardPage("participantWizardPage", this.booking);
		BookingWizardPage bookingPage = new BookingWizardPage("bookingWizardPage", this.booking);
		this.addPage(bookingPage);
		this.addPage(participantPage);
	}

	@Override
	public boolean canFinish()
	{
		for (IWizardPage page : this.getPages())
		{
			if (!page.isPageComplete())
				return false;
		}
		return true;
	}

	public Booking getBooking()
	{
		return this.booking;
	}

	@Override
	public boolean performFinish()
	{
		((BookingWizardPage) this.getPage("bookingWizardPage")).update(this.booking);
		((ParticipantWizardPage) this.getPage("participantWizardPage")).update(this.booking);

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			BookingQuery query = (BookingQuery) service.getQuery(Booking.class);
			this.booking = query.merge(this.booking);
		}
		tracker.close();
		return true;
	}

}
