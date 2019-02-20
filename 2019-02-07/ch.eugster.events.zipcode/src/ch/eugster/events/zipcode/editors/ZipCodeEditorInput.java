package ch.eugster.events.zipcode.editors;

import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class ZipCodeEditorInput extends AbstractEntityEditorInput<ZipCode>
{
	public ZipCodeEditorInput(ZipCode zipCode)
	{
		entity = zipCode;
	}

	@Override
	public boolean hasParent()
	{
		return false;
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
