package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;

public class BookingStateSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof BookingForthcomingState)
		{
			BookingForthcomingState state1 = (BookingForthcomingState) e1;
			BookingForthcomingState state2 = (BookingForthcomingState) e2;
			return new Integer(state1.ordinal()).compareTo(new Integer(state2.ordinal()));
		}
		else if (e1 instanceof BookingDoneState)
		{
			BookingDoneState state1 = (BookingDoneState) e1;
			BookingDoneState state2 = (BookingDoneState) e2;
			return new Integer(state1.ordinal()).compareTo(new Integer(state2.ordinal()));
		}
		else if (e1 instanceof BookingAnnulatedState)
		{
			BookingAnnulatedState state1 = (BookingAnnulatedState) e1;
			BookingAnnulatedState state2 = (BookingAnnulatedState) e2;
			return new Integer(state1.ordinal()).compareTo(new Integer(state2.ordinal()));
		}
		return 0;
	}

}
