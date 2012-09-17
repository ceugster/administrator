package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class LinkPersonAddressEditorInput extends AbstractEntityEditorInput<LinkPersonAddress>
{
	public LinkPersonAddressEditorInput(final LinkPersonAddress link)
	{
		this.entity = link;
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
