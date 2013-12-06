package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.wizards.BookingWizard;
import ch.eugster.events.course.wizards.BookingWizardPage;
import ch.eugster.events.course.wizards.CourseWizardPage;
import ch.eugster.events.course.wizards.ParticipantWizardPage;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.ui.wizards.WizardDialog;

public class AddBookingHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		IStructuredSelection ssel = (IStructuredSelection) context.getParent().getVariable("selection");
		if (!ssel.isEmpty())
		{
			BookingWizard wizard = null;
			if (ssel.getFirstElement() instanceof Course)
			{
				Course course = (Course) ssel.getFirstElement();
				wizard = prepareBooking(course);
			}
			else
			{
				LinkPersonAddress link = null;
				if (ssel.getFirstElement() instanceof Person)
				{
					Person person = (Person) ssel.getFirstElement();
					wizard = prepareBooking(person.getDefaultLink());
				}
				else if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{
					link = (LinkPersonAddress) ssel.getFirstElement();
					wizard = prepareBooking(link);
				}
			}
			if (wizard != null)
			{
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		}
		return Status.OK_STATUS;
	}

	private BookingWizard prepareBooking(final Course course)
	{
		BookingWizard wizard = null;
		Booking booking = Booking.newInstance(course);
		wizard = new BookingWizard(booking);
		BookingWizardPage bookingPage = new BookingWizardPage("bookingWizardPage", wizard);
		wizard.addPage(bookingPage);
		ParticipantWizardPage participantPage = new ParticipantWizardPage("participantWizardPage", wizard);
		wizard.addPage(participantPage);
		return wizard;
	}

	private BookingWizard prepareBooking(LinkPersonAddress link)
	{
		Booking booking = Booking.newInstance();
		Participant participant = Participant.newInstance(link, booking);
		booking.setParticipant(participant);
		booking.addParticipant(participant);
		BookingWizard wizard = new BookingWizard(booking);
		CourseWizardPage coursePage = new CourseWizardPage("courseWizardPage", wizard);
		ParticipantWizardPage participantPage = new ParticipantWizardPage("participantWizardPage", wizard);
		BookingWizardPage bookingPage = new BookingWizardPage("bookingWizardPage", wizard);
		coursePage.addSelectionChangedListener(bookingPage);
		coursePage.addSelectionChangedListener(participantPage);
		wizard.addPage(coursePage);
		wizard.addPage(bookingPage);
		wizard.addPage(participantPage);
		participantPage.addSelectionChangedListener(coursePage);
		return wizard;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		Object sel = context.getVariable("selection");
		if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) sel;
			if (ssel.getFirstElement() instanceof Course)
			{
				Course course = (Course) ssel.getFirstElement();
				enabled = course.getState().equals(CourseState.FORTHCOMING);

			}
			else if (ssel.getFirstElement() instanceof Person)
			{
				enabled = true;
			}
			else if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				enabled = true;
			}
		}
		setBaseEnabled(enabled);
	}

}
