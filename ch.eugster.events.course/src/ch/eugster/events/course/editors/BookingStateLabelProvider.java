package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.IBookingState;

public class BookingStateLabelProvider extends LabelProvider
{

	@Override
	public String getText(Object element)
	{
		if (element instanceof IBookingState)
		{
			return ((IBookingState) element).toString();
		}
		return "";
	}

}
