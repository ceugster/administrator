package ch.eugster.events.country.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Country;

public class CountrySorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof Country && e2 instanceof Country)
		{
			Country country1 = (Country) e1;
			Country country2 = (Country) e2;

			return country1.getName().compareTo(country2.getName());
		}
		return 0;
	}
}
