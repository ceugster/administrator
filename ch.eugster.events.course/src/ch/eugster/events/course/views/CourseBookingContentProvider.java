package ch.eugster.events.course.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Participant;

public class CourseBookingContentProvider implements ITreeContentProvider
{
	@Override
	public Object[] getChildren(Object parent)
	{
		if (parent instanceof Course)
		{
			Course course = (Course) parent;
			return course.getBookings().toArray(new Booking[0]);
		}
		else if (parent instanceof Booking)
		{
			Booking booking = (Booking) parent;
			return booking.getParticipants().toArray(new Participant[0]);
		}
		return new Booking[0];
	}

	@Override
	public Object getParent(Object element)
	{
		if (element instanceof Booking)
		{
			Booking booking = (Booking) element;
			return booking.getCourse();
		}
		else if (element instanceof Participant)
		{
			Participant participant = (Participant) element;
			return participant.getBooking();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element)
	{
		if (element instanceof Course)
		{
			Course course = (Course) element;
			return course.getBookings().size() > 0;
		}
		if (element instanceof Booking)
		{
			Booking booking = (Booking) element;
			return booking.getParticipantCount() > 1;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		return this.getChildren(inputElement);
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

}
