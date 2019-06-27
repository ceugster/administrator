package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.addressgroup.AddressGroupMemberSelector;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;

public class AddressGroupMemberTreeSorter extends ViewerSorter
{
	private final AddressGroupMemberSelector selector;

	public AddressGroupMemberTreeSorter(AddressGroupMemberSelector selector)
	{
		super();
		this.selector = selector;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof AddressGroupCategory)
		{
			if (e2 instanceof AddressGroupCategory)
			{
				AddressGroupCategory category1 = (AddressGroupCategory) e1;
				AddressGroupCategory category2 = (AddressGroupCategory) e2;

				if (category1.getCode().equals(category2.getCode()))
					return category1.getName().compareTo(category2.getName());
				else
					return category1.getCode().compareTo(category2.getCode());
			}
		}
		else if (e1 instanceof AddressGroup && e2 instanceof AddressGroup)
		{
			AddressGroup group1 = (AddressGroup) e1;
			AddressGroup group2 = (AddressGroup) e2;

			boolean checked1 = selector.isChecked(group1);
			boolean checked2 = selector.isChecked(group2);

			if (checked1 == checked2)
			{
				if (group1.getCode().equals(group2.getCode()))
				{
					return group1.getName().compareTo(group2.getName());
				}
				else
				{
					return group1.getCode().compareTo(group2.getCode());
				}
			}
			else if (checked1)
				return -1;
			else if (checked2)
				return 1;
		}

		return 0;
	}
}
