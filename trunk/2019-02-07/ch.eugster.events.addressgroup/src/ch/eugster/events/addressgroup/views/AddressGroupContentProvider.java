package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupContentProvider implements ITreeContentProvider
{
	private ConnectionService connectionService;
	
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getChildren(final Object parentElement)
	{
		if (parentElement instanceof ConnectionService)
		{
			this.connectionService = (ConnectionService) parentElement;
			if (this.connectionService != null)
			{
				DomainQuery query = (DomainQuery) this.connectionService.getQuery(Domain.class);
				return query.selectValids().toArray(new Domain[0]);
			}
		}
		else if (parentElement instanceof Domain)
		{
			if (connectionService != null)
			{
				Domain domain = (Domain) parentElement;
				AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) this.connectionService.getQuery(AddressGroupCategory.class);
				return query.selectByDomain(domain).toArray(new AddressGroupCategory[0]);
			}
		}
		else if (parentElement instanceof AddressGroupCategory)
		{
			return ((AddressGroupCategory) parentElement).getAddressGroups().toArray(new AddressGroup[0]);
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(final Object inputElement)
	{
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(final Object element)
	{
		if (element instanceof AddressGroup)
		{
			return ((AddressGroup) element).getAddressGroupCategory();
		}
		else if (element instanceof AddressGroupCategory)
		{
			return ((AddressGroupCategory) element).getDomain();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element)
	{
		if (element instanceof ConnectionService)
		{
			this.connectionService = (ConnectionService) element;
			DomainQuery query = (DomainQuery) this.connectionService.getQuery(Domain.class);
			return query.selectValids().size() > 0;
		}
		if (element instanceof Domain)
		{
			if (this.connectionService != null)
			{
				Domain domain = (Domain) element;
				AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) this.connectionService.getQuery(AddressGroupCategory.class);
				return query.countByDomain(domain) > 0L;
			}
		}
		else if (element instanceof AddressGroupCategory)
		{
			return ((AddressGroupCategory) element).getAddressGroups().size() > 0;
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}

}
