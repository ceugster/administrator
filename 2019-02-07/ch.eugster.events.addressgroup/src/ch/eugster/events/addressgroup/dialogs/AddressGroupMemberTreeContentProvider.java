package ch.eugster.events.addressgroup.dialogs;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupMemberTreeContentProvider implements ITreeContentProvider
{
	private ConnectionService connectionService;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	public AddressGroupMemberTreeContentProvider()
	{
		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		connectionServiceTracker.open();
		connectionService = (ConnectionService) connectionServiceTracker.getService();
	}

	@Override
	public Object[] getElements(Object object)
	{
		return this.getChildren(object);
	}

	@Override
	public Object[] getChildren(Object object)
	{
		if (object instanceof Domain)
		{
			AddressGroupCategory[] categories = new AddressGroupCategory[0];
			Domain domain = (Domain) object;
			AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) connectionService
					.getQuery(AddressGroupCategory.class);
			categories = query.selectByDomain(domain).toArray(new AddressGroupCategory[0]);
			return categories;
		}
		else if (object instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) object;
			AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) connectionService
					.getQuery(AddressGroupCategory.class);
			try
			{
				category = (AddressGroupCategory) query.refresh(category);
				return category.getAddressGroups().toArray(new AddressGroup[0]);
			}
			catch (Exception e)
			{
				category = query.find(AddressGroupCategory.class, category.getId());
				return category.getAddressGroups().toArray(new AddressGroup[0]);
			}
		}
		return new AddressGroup[0];
	}

	@Override
	public boolean hasChildren(Object object)
	{
		if (object instanceof Domain)
		{
			Domain domain = (Domain) object;
			AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) connectionService
					.getQuery(AddressGroupCategory.class);
			return query.countByDomain(domain) > 0;
		}
		else if (object instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) object;
			AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) connectionService
					.getQuery(AddressGroupCategory.class);
			try
			{
				category = (AddressGroupCategory) query.refresh(category);
			}
			catch (Exception e)
			{
				category = query.find(AddressGroupCategory.class, category.getId());
			}
			return category.getAddressGroups().size() > 0;
		}
		return false;
	}

	@Override
	public Object getParent(Object object)
	{
		if (object instanceof AddressGroupCategory)
		{
			return ((AddressGroupCategory) object).getDomain();
		}
		else if (object instanceof AddressGroup)
		{
			return ((AddressGroup) object).getAddressGroupCategory();
		}
		else if (object instanceof AddressGroupMember)
		{
			return ((AddressGroupMember) object).getAddressGroup();
		}
		return null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();
	}

}
