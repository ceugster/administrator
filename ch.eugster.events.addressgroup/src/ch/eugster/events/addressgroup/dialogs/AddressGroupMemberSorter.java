package ch.eugster.events.addressgroup.dialogs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.AddressGroup;

public class AddressGroupMemberSorter extends ViewerSorter
{
	private final AddressGroupMemberDialog dialog;

	public AddressGroupMemberSorter(AddressGroupMemberDialog dialog)
	{
		super();
		this.dialog = dialog;
	}

	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{
		if (element1 instanceof AddressGroup && element2 instanceof AddressGroup)
		{
			AddressGroup addressGroup1 = (AddressGroup) element1;
			AddressGroup addressGroup2 = (AddressGroup) element2;

			boolean checked1 = dialog.isChecked(addressGroup1);
			boolean checked2 = dialog.isChecked(addressGroup2);

			if (checked1 && checked2)
				return addressGroup1.getCode().compareTo(addressGroup2.getCode());
			else if (checked1)
				return -1;
			else if (checked2)
				return 1;
			else
				return addressGroup1.getCode().compareTo(addressGroup2.getCode());
		}

		return 0;
	}
}
