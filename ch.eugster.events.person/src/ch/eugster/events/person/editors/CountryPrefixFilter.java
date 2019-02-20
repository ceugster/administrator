package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Country;

public class CountryPrefixFilter extends ViewerFilter
{

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (element instanceof Country)
		{
			return ((Country) element).getPhonePrefix() != null;
		}
		return true;
	}

}
