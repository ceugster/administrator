package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class AddressGroupMemberNameFilter extends ViewerFilter
{
	private String value = "";

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element)
	{
		if (element instanceof AddressGroupMember)
		{
			if (this.value.isEmpty())
				return true;

			AddressGroupMember member = (AddressGroupMember) element;
			if (member.getLink() == null || member.getLink().isDeleted() || member.getLink().getPerson().isDeleted())
			{
				String name = member.getAddress().getName();
				return name.toLowerCase().contains(this.value.toLowerCase());
			}
			else
			{
				String name = PersonFormatter.getInstance().formatLastnameFirstname(member.getLink().getPerson());
				return name.toLowerCase().contains(this.value.toLowerCase());
			}
		}
		return true;
	}

	public void setFilter(final String value)
	{
		this.value = value;
	}

}
