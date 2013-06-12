package ch.eugster.events.course.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.wizards.BookingWizard;
import ch.eugster.events.course.wizards.BookingWizardPage;
import ch.eugster.events.course.wizards.CourseWizardPage;
import ch.eugster.events.course.wizards.ParticipantWizardPage;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.ui.wizards.WizardDialog;

public class AddBookingHandler extends AbstractHandler implements IHandler
{

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
		if (!ssel.isEmpty())
		{
			Wizard wizard = null;
			if (ssel.getFirstElement() instanceof Course)
			{
				Booking booking = Booking.newInstance((Course) ssel.getFirstElement());
				wizard = new BookingWizard(booking);
				ParticipantWizardPage participantPage = new ParticipantWizardPage("participantWizardPage", booking);
				wizard.addPage(participantPage);
				BookingWizardPage bookingPage = new BookingWizardPage("bookingWizardPage", booking);
				wizard.addPage(bookingPage);
			}
			else
			{
				Booking booking = Booking.newInstance();

				Collection<Participant> participants = new ArrayList<Participant>();
				List<AbstractEntity> entities = ssel.toList();
				for (AbstractEntity entity : entities)
				{
					if (entity instanceof LinkPersonAddress)
					{
						participants.add(Participant.newInstance((LinkPersonAddress) entity, booking));
					}
					else if (entity instanceof Person)
					{
						Person person = (Person) entity;
						if (person.getDefaultLink() != null)
						{
							participants.add(Participant.newInstance(person.getDefaultLink(), booking));
						}
					}
				}
				if (participants.size() > 0)
				{
					booking.setParticipants(participants);
					booking.setParticipant(participants.toArray(new Participant[0])[0]);
					wizard = new BookingWizard(booking);
					CourseWizardPage coursePage = new CourseWizardPage("courseWizardPage", booking);
					ParticipantWizardPage participantPage = new ParticipantWizardPage("participantWizardPage", booking);
					BookingWizardPage bookingPage = new BookingWizardPage("bookingWizardPage", booking);
					coursePage.addSelectionChangedListener(bookingPage);
					coursePage.addSelectionChangedListener(participantPage);
					wizard.addPage(coursePage);
					wizard.addPage(bookingPage);
					wizard.addPage(participantPage);
					participantPage.addSelectionChangedListener(coursePage);
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

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		Object sel = context.getVariable("selection");
		if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) sel;
			if (ssel.getFirstElement() instanceof Course)
			{
				Course course = (Course) ssel.getFirstElement();
				setBaseEnabled(course.getParticipantsCount() < course.getMaxParticipants());
			}
		}
	}

}
