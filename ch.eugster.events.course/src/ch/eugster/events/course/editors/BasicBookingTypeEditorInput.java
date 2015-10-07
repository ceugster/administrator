package ch.eugster.events.course.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.BookingTypeProposition;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class BasicBookingTypeEditorInput extends AbstractEntityEditorInput<BookingTypeProposition>
{
	public BasicBookingTypeEditorInput(BookingTypeProposition basicBookingType)
	{
		this.entity = basicBookingType;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public AbstractEntity getParent()
	{
		return null;
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
