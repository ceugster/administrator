package ch.eugster.events.course.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.ui.helpers.EmailHelper;

public class SendEmailHandler extends AbstractHandler implements IHandler
{
	private final List<String> addresses = new ArrayList<String>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (EmailHelper.getInstance().isEmailSupported())
		{
			if (!this.addresses.isEmpty())
				this.addresses.clear();

			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) event.getApplicationContext();
				ISelection sel = (ISelection) context.getParent().getVariable("selection");
				if (sel instanceof StructuredSelection)
				{
					StructuredSelection ssel = (StructuredSelection) sel;
					Iterator<?> iterator = ssel.iterator();
					while (iterator.hasNext())
					{
						Object element = iterator.next();
						if (element instanceof Season)
						{
							this.extract((Season) element);
						}
						else if (element instanceof Course)
						{
							this.extract((Course) element);
						}
						else if (element instanceof Booking)
						{
							this.extract((Booking) element);
						}
						else if (element instanceof Participant)
						{
							this.extract((Participant) element);
						}
					}

					if (!this.addresses.isEmpty())
						EmailHelper.getInstance().sendEmail(this.addresses.toArray(new String[0]));
				}
			}
		}
		return null;
	}

	private void extract(Season season)
	{
		if (!season.isDeleted())
		{
			List<Course> courses = season.getCourses();
			for (Course course : courses)
			{
				this.extract(course);
			}
		}
	}

	private void extract(Course course)
	{
		if (!course.isDeleted())
		{
			List<Booking> bookings = course.getBookings();
			for (Booking booking : bookings)
			{
				this.extract(booking);
			}
		}
	}

	private void extract(Booking booking)
	{
		if (!booking.isDeleted())
		{
			List<Participant> participants = booking.getParticipants();
			for (Participant participant : participants)
			{
				if (!participant.isDeleted())
				{
					this.extract(participant);
				}
			}
		}
	}

	private void extract(Participant participant)
	{
		if (!participant.isDeleted())
		{
			if (!participant.getLink().isDeleted() && !participant.getLink().getPerson().isDeleted() && !participant.getLink().getAddress().isDeleted())
			{
				if (EmailHelper.getInstance().isValidAddress(participant.getLink().getPerson().getEmail()))
				{
					addEmail(participant.getLink().getPerson().getEmail());
				}
				else if (EmailHelper.getInstance().isValidAddress(participant.getLink().getEmail()))
				{
					addEmail(participant.getLink().getEmail());
				}
				else if (EmailHelper.getInstance().isValidAddress(participant.getLink().getAddress().getEmail()))
				{
					addEmail(participant.getLink().getAddress().getEmail());
				}
			}
		}
	}

	private void addEmail(String email)
	{
		if (!email.isEmpty() && !this.addresses.contains(email))
		{
			this.addresses.add(email);
		}
	}
}
