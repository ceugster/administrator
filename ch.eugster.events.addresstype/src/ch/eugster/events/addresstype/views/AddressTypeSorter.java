package ch.eugster.events.addresstype.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.AddressType;

public class AddressTypeSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		AddressType at1 = (AddressType) e1;
		AddressType at2 = (AddressType) e2;

		if (at1.getName().equals("") && at2.getName().equals(""))
			return 0;
		else if (at1.getName().isEmpty())
		{
			return -1;
		}
		else if (at2.getName().isEmpty())
		{
			return 1;
		}
		else
		{
			return at1.getName().compareTo(at2.getName());
		}
	}
}
