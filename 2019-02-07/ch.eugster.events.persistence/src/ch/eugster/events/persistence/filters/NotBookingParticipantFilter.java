package ch.eugster.events.persistence.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Participant;

public class NotBookingParticipantFilter extends ViewerFilter
{

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (element instanceof Participant)
		{
			Participant participant = (Participant) element;
			if (participant.getBooking().getParticipant().getId().equals(participant.getId()))
			{
				return false;
			}
		}
		return true;
	}

}
