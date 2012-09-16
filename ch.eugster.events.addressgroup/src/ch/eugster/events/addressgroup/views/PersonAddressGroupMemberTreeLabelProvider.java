package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.addressgroup.AddressGroupFormatter;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;

public class PersonAddressGroupMemberTreeLabelProvider extends LabelProvider
{
	@Override
	public String getText(Object element)
	{
		if (element instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) element;
			return AddressGroupFormatter.getInstance().formatAddressGroupCategoryTreeLabel(category);
		}
		else if (element instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) element;
			return AddressGroupFormatter.getInstance().formatAddressGroupTreeLabel(addressGroup);
		}
		return "";
	}

	@Override
	public Image getImage(Object object)
	{
		return null;
	}

	@Override
	public void dispose() {}

}
