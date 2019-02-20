package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Address;

public class AddressFilter extends ViewerFilter
{

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element)
	{
		if (element instanceof Address)
		{
			Address address = (Address) element;
			return address.getPersonLinks().isEmpty();
		}
		return true;
	}

}
