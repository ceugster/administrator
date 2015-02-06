package ch.eugster.events.course.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;

public class LinkParticipantContentProvider implements ITreeContentProvider
{
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getChildren(final Object parent)
	{
		if (parent instanceof Person)
		{
			Person person = (Person) parent;
			Map<Long, Booking> bookings = new HashMap<Long, Booking>();
			List<Participant> participants = person.getDefaultLink().getParticipants();
			for (Participant participant : participants)
			{
				if (participant.getBooking() != null)
				{
					if (bookings.get(participant.getBooking().getId()) == null)
					{
						bookings.put(participant.getBooking().getId(), participant.getBooking());
					}
				}
			}
			return bookings.values().toArray(new Booking[0]);
		}
		else if (parent instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) parent;
			List<Participant> participants = link.getParticipants();
			List<Booking> bookings = new ArrayList<Booking>();
			for (Participant participant : participants)
			{
				if (!bookings.contains(participant.getBooking()))
				{
					bookings.add(participant.getBooking());
				}
			}
			return bookings.toArray(new Booking[0]);
		}
		else if (parent instanceof Booking)
		{
			Booking booking = (Booking) parent;
			return booking.getParticipants().toArray(new Participant[0]);
		}
		return new Booking[0];
	}

	@Override
	public Object[] getElements(final Object inputElement)
	{
		return this.getChildren(inputElement);
	}

	@Override
	public Object getParent(final Object element)
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
	public boolean hasChildren(final Object element)
	{
		if (element instanceof Person)
		{
			Person person = (Person) element;
			return person.getParticipants().size() > 0;
		}
		else if (element instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) element;
			return link.getParticipants().size() > 0;
		}
		else if (element instanceof Booking)
		{
			Booking booking = (Booking) element;
			return booking.getParticipants().size() > 0;
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}

}
