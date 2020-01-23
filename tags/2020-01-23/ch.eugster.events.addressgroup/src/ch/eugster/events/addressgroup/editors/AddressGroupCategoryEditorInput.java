package ch.eugster.events.addressgroup.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressGroupCategoryEditorInput extends
AbstractEntityEditorInput<AddressGroupCategory>
{

	public AddressGroupCategoryEditorInput(AddressGroupCategory category)
	{
		this.entity = category;
	}

	@Override
	public boolean hasParent()
	{
		return true;
	}

	@Override
	public AbstractEntity getParent()
	{
		return this.entity.getDomain();
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
