package ch.eugster.events.persistence.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class DeletedEntityFilter extends ViewerFilter
{
	private boolean select(final AddressGroup addressGroup)
	{
		return addressGroup.isValid();
	}

	private boolean select(final AddressGroupMember member)
	{
		return member.isValid();
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
