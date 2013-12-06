package ch.eugster.events.addressgroup.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressGroupEditorInput extends
AbstractEntityEditorInput<AddressGroup>
{

	public AddressGroupEditorInput(AddressGroup addressGroup)
	{
		this.entity = addressGroup;
	}

	@Override
	public boolean hasParent()
	{
		return true;
	}

	@Override
	public AbstractEntity getParent()
	{
		return this.entity.getAddressGroupCategory();
	}

	@Override
	public String getName()
	{
		return "TTT";
	}

	@Override
	public String getToolTipText()
	{
		return "TTT";
	}

}
