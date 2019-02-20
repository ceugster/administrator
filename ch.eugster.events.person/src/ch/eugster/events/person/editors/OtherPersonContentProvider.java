package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;

public class OtherPersonContentProvider implements IStructuredContentProvider
{
	@Override
	public Object[] getElements(Object object)
	{
		if (object instanceof Address)
		{
			return ((Address) object).getPersonLinks().toArray(new LinkPersonAddress[0]);
		}

		return new LinkPersonAddress[0];
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

}
