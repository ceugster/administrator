package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;

public class AddressGroupLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(final Object element)
	{
		if (element instanceof AddressGroupCategory)
		{
			return Activator.getDefault().getImageRegistry().get("CATEGORY");
		}
		else if (element instanceof AddressGroup)
		{
			return Activator.getDefault().getImageRegistry().get("ADDRESS_GROUP");
		}
		// else if (element instanceof AddressGroupLink)
		// {
		// return
		// Activator.getDefault().getImageRegistry().get("ADDRESS_GROUP");
		// }
		return null;
	}

	@Override
	public String getText(final Object element)
	{
		if (element instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) element;
			return category.getCode().equals("") ? category.getName() : category.getCode() + " - " + category.getName();
		}
		else if (element instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) element;
			return addressGroup.getCode().equals("") ? addressGroup.getName() : addressGroup.getCode() + " - "
					+ addressGroup.getName();
		}
		// else if (element instanceof AddressGroupLink)
		// {
		// AddressGroupLink link = (AddressGroupLink) element;
		// return link.getChild().getCode().equals("") ?
		// link.getChild().getName() : link.getChild().getCode() + " - "
		// + link.getChild().getName();
		// }
		else
			return "";
	}

}
