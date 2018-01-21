package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.Domain;

public class AddressGroupLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(final Object element)
	{
		if (element instanceof Domain)
		{
			return Activator.getDefault().getImageRegistry().get("DOMAIN");
		}
		if (element instanceof AddressGroupCategory)
		{
			return Activator.getDefault().getImageRegistry().get("CATEGORY");
		}
		if (element instanceof AddressGroup)
		{
			return Activator.getDefault().getImageRegistry().get("ADDRESS_GROUP");
		}
		return null;
	}

	@Override
	public String getText(final Object element)
	{
		if (element instanceof Domain)
		{
			Domain domain = (Domain) element;
			return domain.getComboFormat();
		}
		if (element instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) element;
			return category.getCode().equals("") ? category.getName() : category.getCode() + " - " + category.getName();
		}
		if (element instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) element;
			return addressGroup.getCode().equals("") ? addressGroup.getName() : addressGroup.getCode() + " - "
					+ addressGroup.getName();
		}
		return "";
	}

}
