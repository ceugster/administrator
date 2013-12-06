package ch.eugster.events.course.wizards;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;

public class PersonBookingViewerContentProvider extends ArrayContentProvider
{

	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof LinkPersonAddress)
		{
			Collection<Booking> bookings = new ArrayList<Booking>();
			Collection<Participant> participants = ((LinkPersonAddress) inputElement).getParticipants();
			for (Participant participant : participants)
				if (!bookings.contains(participant.getBooking()))
					bookings.add(participant.getBooking());
			return bookings.toArray(new Booking[0]);
		}
		return new Booking[0];
	}

}
