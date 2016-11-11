package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.addressgroup.AddressGroupMemberSelector;
import ch.eugster.events.persistence.model.AddressGroup;

public class AddressGroupMemberTreeSorter extends ViewerSorter
{
	private final AddressGroupMemberSelector selector;

	public AddressGroupMemberTreeSorter(AddressGroupMemberSelector selector)
	{
		super();
		this.selector = selector;
	}

	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{
		if (element1 instanceof AddressGroup && element2 instanceof AddressGroup)
		{
			AddressGroup addressGroup1 = (AddressGroup) element1;
			AddressGroup addressGroup2 = (AddressGroup) element2;

			boolean checked1 = selector.isChecked(addressGroup1);
			boolean checked2 = selector.isChecked(addressGroup2);

			if (checked1 == checked2)
				return addressGroup1.getCode().compareTo(addressGroup2.getCode());
			else if (checked1)
				return -1;
			else if (checked2)
				return 1;
		}

		return 0;
	}
}
