package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.AddressType;

public class AddressTypeSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{
		AddressType ad1 = (AddressType) element1;
		AddressType ad2 = (AddressType) element2;

		return ad1.getName().compareTo(ad2.getName());
	}
}
