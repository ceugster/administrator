package ch.eugster.events.course.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class BookingEditorInput extends AbstractEntityEditorInput<Booking>
{
	public BookingEditorInput(Booking booking)
	{
		this.entity = booking;
	}

	@Override
	public boolean hasParent()
	{
		return true;
	}

	@Override
	public AbstractEntity getParent()
	{
		return this.entity.getCourse();
	}

	@Override
	public String getName()
	{
		return "TTT";
	}

	@Override
	public String getToolTipText()
	{

		return "FFF";
	}
}
