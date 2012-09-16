package ch.eugster.events.course.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.Participant;

public class CourseBookingSorter extends ViewerSorter
{
	private final int columnIndex = 0;

	private final Order order = Order.ASCENDING;

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		if (e1 instanceof Booking && e2 instanceof Booking)
		{
			Booking booking1 = (Booking) e1;
			Booking booking2 = (Booking) e2;

			return this.compareBooking(booking1, booking2);
		}
		else if (e1 instanceof Participant && e2 instanceof Participant)
		{
			Participant participant1 = (Participant) e1;
			Participant participant2 = (Participant) e2;

			return this.compareParticipant(participant1, participant2);
		}
		return 0;
	}

	private int compareBooking(final Booking booking1, final Booking booking2)
	{
		switch (this.columnIndex)
		{
			case 0:
			{
				return this.compareDate(booking1, booking2);
			}
			case 1:
			{
				return this.compareState(booking1, booking2);
			}

		}
		return 0;
	}

	private int compareDate(final Booking booking1, final Booking booking2)
	{
		if (this.order.equals(Order.ASCENDING))
			return booking1.getDate().compareTo(booking2.getDate());
		else if (this.order.equals(Order.DESCENDING))
			return booking2.getDate().compareTo(booking1.getDate());
		else
			return 0;
	}

	private int compareDate(final Participant participant1, final Participant participant2)
	{
		if (this.order.equals(Order.ASCENDING))
			return participant1.getDate().compareTo(participant2.getDate());
		else if (this.order.equals(Order.DESCENDING))
			return participant2.getDate().compareTo(participant1.getDate());
		else
			return 0;
	}

	private int compareName(final Participant participant1, final Participant participant2)
	{
		String name1 = PersonFormatter.getInstance().formatLastnameFirstname(participant1.getLink().getPerson());
		String name2 = PersonFormatter.getInstance().formatLastnameFirstname(participant2.getLink().getPerson());
		if (this.order.equals(Order.ASCENDING))
			return name1.compareTo(name2);
		else
			return name2.compareTo(name1);
	}

	private int compareParticipant(final Participant participant1, final Participant participant2)
	{
		switch (this.columnIndex)
		{
			case 0:
			{
				return this.compareDate(participant1, participant2);
			}
			case 1:
			{
				return this.compareName(participant1, participant2);
			}

		}
		return 0;
	}

	private int compareState(final Booking booking1, final Booking booking2)
	{
		CourseState state = booking1.getCourse().getState();
		if (this.order.equals(Order.ASCENDING))
		{
			if (state.equals(CourseState.FORTHCOMING))
				return booking1.getForthcomingState().compareTo(booking2.getForthcomingState());
			else if (state.equals(CourseState.DONE))
				return booking1.getDoneState().compareTo(booking2.getDoneState());
			else if (state.equals(CourseState.ANNULATED))
				return booking1.getAnnulatedState().compareTo(booking2.getAnnulatedState());
		}
		else if (this.order.equals(Order.DESCENDING))
		{
			if (state.equals(CourseState.FORTHCOMING))
				return booking2.getForthcomingState().compareTo(booking1.getForthcomingState());
			else if (state.equals(CourseState.DONE))
				return booking2.getDoneState().compareTo(booking1.getDoneState());
			else if (state.equals(CourseState.ANNULATED))
				return booking2.getAnnulatedState().compareTo(booking1.getAnnulatedState());
		}

		return 0;
	}

	private enum Order
	{
		ASCENDING, DESCENDING;
	}
}