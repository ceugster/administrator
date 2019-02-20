package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Country;

public class CountryPrefixSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof Country && e2 instanceof Country)
		{
			Country c1 = (Country) e1;
			Country c2 = (Country) e2;

			return c1.getPhonePrefix().compareTo(c2.getPhonePrefix());
		}
		return 0;
	}

}
