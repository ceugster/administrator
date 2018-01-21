package ch.eugster.events.addressgroup.views;

import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.AddressGroupFormatter;
import ch.eugster.events.addressgroup.Monitor;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.Domain;

public class PersonAddressGroupMemberTreeLabelProvider extends LabelProvider
{
	private Map<Long, Monitor> monitors;
	
	public PersonAddressGroupMemberTreeLabelProvider(Map<Long, Monitor> monitors)
	{
		this.monitors = monitors;
	}
	
	@Override
	public String getText(Object element)
	{
		if (element instanceof Domain)
		{
			Domain domain = (Domain) element;
			return domain.getComboFormat();
		}
		else if (element instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) element;
			return AddressGroupFormatter.getInstance().formatAddressGroupCategoryTreeLabel(category);
		}
		else if (element instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) element;
			String text = AddressGroupFormatter.getInstance().formatAddressGroupTreeLabel(addressGroup);
			Monitor monitor = monitors.get(addressGroup.getId());
			if (monitor != null && monitor.addressGroupMember != null && !monitor.addressGroupMember.isDeleted())
			{
				if (monitor.addressGroupMember.isValidAddressMember())
				{
					text += " (Adresse" + (monitor.addressGroupMember.getAddress().getName().isEmpty() ? ")" : ": " + monitor.addressGroupMember.getAddress().getName() + ")");
				}
				else if (monitor.addressGroupMember.isValidLinkMember())
				{
					text += " (Person: " + PersonFormatter.getInstance().formatLastnameFirstname(monitor.addressGroupMember.getLink().getPerson()) + ")";
				}
			}
			return text;
		}
		return "";
	}

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof Domain)
		{
			return Activator.getDefault().getImageRegistry().get("DOMAIN");
		}
		else if (element instanceof AddressGroupCategory)
		{
			return Activator.getDefault().getImageRegistry().get("CATEGORY");
		}
		else if (element instanceof AddressGroup)
		{
			return Activator.getDefault().getImageRegistry().get("ADDRESS_GROUP");
		}
		return null;
	}

	@Override
	public void dispose() {}

}
