/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.person.views;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.views.PersonView.ContentRoot;

public class PersonContentProvider implements ITreeContentProvider
{
	private ContentRoot root;

	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getChildren(final Object parentElement)
	{
		if (parentElement instanceof ContentRoot)
		{
			root = (ContentRoot) parentElement;
			return root.getEntities();
		}
		else if (parentElement instanceof Person)
		{
			Collection<LinkPersonAddress> secondaryAddresses = new ArrayList<LinkPersonAddress>();
			Collection<LinkPersonAddress> links = ((Person) parentElement).getLinks();
			for (LinkPersonAddress link : links)
			{
				if (link.getPerson().getDefaultLink() == null
						|| !link.getId().equals(link.getPerson().getDefaultLink().getId()))
				{
					secondaryAddresses.add(link);
				}
			}
			return secondaryAddresses.toArray(new LinkPersonAddress[0]);
		}
		else if (parentElement instanceof Address)
		{
			Address address = (Address) parentElement;
			return address.getPersonLinks().toArray(new LinkPersonAddress[0]);
		}
		else
			return new AbstractEntity[0];
	}

	@Override
	public Object[] getElements(final Object object)
	{
		return getChildren(object);
	}

	@Override
	public Object getParent(final Object element)
	{
		if (element instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) element;
			// if (root.getEntities()[0] instanceof Address)
			// {
			// return link.getAddress();
			// }
			// else
			// {
			return link.getPerson();
			// }
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element)
	{
		if (element instanceof ContentRoot)
		{
			return ((ContentRoot) element).getEntities().length > 0;
		}
		else if (element instanceof Person)
		{
			int count = 0;
			Collection<LinkPersonAddress> links = ((Person) element).getLinks();
			for (LinkPersonAddress link : links)
			{
				if (link.getPerson().getDefaultLink() == null
						|| !link.getId().equals(link.getPerson().getDefaultLink().getId()))
				{
					if (!link.isDeleted())
					{
						count++;
					}
				}
			}
			return count > 0;
		}
		else if (element instanceof Address)
		{
			return ((Address) element).getPersonLinks().size() > 0;
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}

}
