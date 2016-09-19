package ch.eugster.events.addressgroup.views;

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

public class PersonAddressGroupMemberTreeContentProvider implements ITreeContentProvider
{
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
			ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle()
					.getBundleContext(), ConnectionService.class, null);
			connectionServiceTracker.open();
			try
			{
				ConnectionService con = (ConnectionService) connectionServiceTracker.getService();
				if (con != null)
				{
					Domain domain = (Domain) object;
					AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) con.getQuery(AddressGroupCategory.class);
					categories = query.selectByDomain(domain).toArray(new AddressGroupCategory[0]);
				}
			}
			finally
			{
				connectionServiceTracker.close();
			}
			return categories;
		}
		else if (object instanceof AddressGroupCategory)
		{
			return ((AddressGroupCategory) object).getAddressGroups().toArray(new AddressGroup[0]);
		}
		return new AddressGroupMember[0];
	}

	@Override
	public boolean hasChildren(Object object)
	{
		if (object instanceof Domain)
		{
			long count = 0l;
			ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle()
					.getBundleContext(), ConnectionService.class, null);
			connectionServiceTracker.open();
			try
			{
				ConnectionService con = (ConnectionService) connectionServiceTracker.getService();
				if (con != null)
				{
					Domain domain = (Domain) object;
					AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) con.getQuery(AddressGroupCategory.class);
					count = query.countByDomain(domain);
				}
			}
			finally
			{
				connectionServiceTracker.close();
			}
			return count > 0;
		}
		else if (object instanceof AddressGroupCategory)
		{
			return ((AddressGroupCategory) object).getAddressGroups().size() > 0;
		}
		else if (object instanceof AddressGroup)
		{
			return false;
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
	}

}
