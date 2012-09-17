package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.AddressSalutation;

public class AddressSalutationSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof AddressSalutation && e2 instanceof AddressSalutation)
		{
			AddressSalutation salutation1 = (AddressSalutation) e1;
			AddressSalutation salutation2 = (AddressSalutation) e2;
			return salutation1.getSalutation().compareTo(salutation2.getSalutation());
		}
		else if (e1 instanceof String && e2 instanceof String)
		{
			String salutation1 = (String) e1;
			String salutation2 = (String) e2;
			return salutation1.compareTo(salutation2);
		}
		return 0;
	}
}
