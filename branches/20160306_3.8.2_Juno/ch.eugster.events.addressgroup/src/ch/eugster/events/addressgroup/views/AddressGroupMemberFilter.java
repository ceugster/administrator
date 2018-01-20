package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;

public class AddressGroupMemberFilter extends ViewerFilter
{
	private String value = "";

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element)
	{
		if (element instanceof AddressGroupMember)
		{
			boolean filter = true;
			if (this.value.isEmpty())
				return filter;

			AddressGroupMember member = (AddressGroupMember) element;
			if (member.isValidAddressMember())
			{
				Address address = member.getAddress();
				String name = address.getName();
				if (name.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String street = address.getAddress();
				if (street.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String zip = address.getZip();
				if (zip.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String city = address.getCity();
				if (city.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String email = address.getEmail();
				if (email.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				return false;
			}
			else if (member.isValidLinkMember())
			{
				LinkPersonAddress link = member.getLink();
				String name = PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
				if (name.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String organisation = link.getAddress().getName();
				if (organisation.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String street = link.getAddress().getAddress();
				if (street.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String zip = link.getAddress().getZip();
				if (zip.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String city = link.getAddress().getCity();
				if (city.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				String email = link.getEmail();
				if (email.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				email = link.getPerson().getEmail();
				if (email.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				email = link.getAddress().getEmail();
				if (email.toLowerCase().contains(this.value.toLowerCase()))
				{
					return true;
				}
				return false;
			}
			
		}
		return true;
	}

	public void setFilter(final String value)
	{
		this.value = value;
	}

}
