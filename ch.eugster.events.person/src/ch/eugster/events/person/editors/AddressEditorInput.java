package ch.eugster.events.person.editors;

import java.util.HashMap;
import java.util.Map;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressEditorInput extends AbstractEntityEditorInput<Address> implements Initializable
{
	private Map<String, String> initialValues;

	public AddressEditorInput(Address address)
	{
		this(address, new HashMap<String, String>());
	}

	public AddressEditorInput(Address address, Map<String, String> initialValues)
	{
		this.entity = address;
		this.initialValues = initialValues;
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

	@Override
	public Map<String, String> getInitialValues()
	{
		return initialValues;
	}

}
