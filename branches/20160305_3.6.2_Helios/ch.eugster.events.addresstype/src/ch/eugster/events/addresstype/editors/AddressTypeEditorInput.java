package ch.eugster.events.addresstype.editors;

import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressTypeEditorInput extends AbstractEntityEditorInput<AddressType>
{
	public AddressTypeEditorInput(AddressType addressType)
	{
		entity = addressType;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return "Adressart";
	}

	@Override
	public String getToolTipText()
	{

		return "Adressart";
	}
}
