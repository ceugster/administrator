package ch.eugster.events.persistence.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;

public class DeletedEntityFilter extends ViewerFilter
{

	private boolean select(final Address address)
	{
		return address == null ? true : !address.isDeleted();
	}

	private boolean select(final AddressGroup addressGroup)
	{
		return !addressGroup.isDeleted() && !addressGroup.getAddressGroupCategory().isDeleted();
	}

	private boolean select(final AddressGroupMember member)
	{
		return !member.isDeleted() && select(member.getAddressGroup()) && select(member.getLink())
				&& select(member.getAddress());
	}

	private boolean select(final LinkPersonAddress link)
	{
		return link == null ? true : (!link.isDeleted() && !link.getPerson().isDeleted());
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element)
	{
		if (element instanceof AddressGroupMember)
		{
			return select((AddressGroupMember) element);
		}
		if (element instanceof AddressGroup)
		{
			return select((AddressGroup) element);
		}
		if (element instanceof AddressGroupCategory)
		{
			return !((AddressGroupCategory) element).isDeleted();
		}
		if (parentElement instanceof AbstractEntity)
		{
			if (((AbstractEntity) parentElement).isDeleted())
			{
				return false;
			}
		}
		if (element instanceof AbstractEntity)
		{
			return !((AbstractEntity) element).isDeleted();
		}
		return true;
	}
}
