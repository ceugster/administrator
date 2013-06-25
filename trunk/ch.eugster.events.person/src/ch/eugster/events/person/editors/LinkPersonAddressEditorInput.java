package ch.eugster.events.person.editors;

import java.util.HashMap;
import java.util.Map;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class LinkPersonAddressEditorInput extends AbstractEntityEditorInput<LinkPersonAddress> implements Initializable
{
	private Map<String, String> initialValues;

	public LinkPersonAddressEditorInput(final LinkPersonAddress link)
	{
		this(link, new HashMap<String, String>());
	}

	public LinkPersonAddressEditorInput(final LinkPersonAddress link, Map<String, String> initialValues)
	{
		this.entity = link;
		this.initialValues = initialValues == null ? new HashMap<String, String>() : initialValues;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class clazz)
	{
		if (clazz.equals(LinkPersonAddress.class))
		{
			return this.entity;
		}
		return super.getAdapter(clazz);
	}

	public Map<String, String> getInitialValues()
	{
		return initialValues;
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
			return PersonFormatter.getInstance().formatLastnameFirstname(entity.getPerson());
		}
	}

	@Override
	public String getToolTipText()
	{
		return getName();
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

}
