package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressEditorInput extends AbstractEntityEditorInput<Address>
{
	public AddressEditorInput(Address address)
	{
		this.entity = address;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		if (entity.getId() == null)
		{
			return "Neu";
		}
		else
		{
			return entity.getId().toString();
		}
	}

	@Override
	public String getToolTipText()
	{
		return getName();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class clazz)
	{
		if (clazz.equals(Address.class))
		{
			return this.entity;
		}
		return super.getAdapter(clazz);
	}

}
