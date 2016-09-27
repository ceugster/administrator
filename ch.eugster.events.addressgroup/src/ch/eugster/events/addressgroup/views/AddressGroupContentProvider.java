package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupContentProvider implements ITreeContentProvider
{

	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getChildren(final Object parentElement)
	{
		if (parentElement instanceof Domain)
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
					Domain domain = (Domain) parentElement;
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
		else if (parentElement instanceof AddressGroupCategory)
		{
			return ((AddressGroupCategory) parentElement).getAddressGroups().toArray(new AddressGroup[0]);
		}
		// else if (parentElement instanceof AddressGroup)
		// {
		// return ((AddressGroup) parentElement).getChildren().toArray(new
		// AddressGroupLink[0]);
		// }
		return new AddressGroupCategory[0];
	}

	@Override
	public Object[] getElements(final Object inputElement)
	{
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(final Object element)
	{
		// if (element instanceof AddressGroupLink)
		// {
		// return ((AddressGroupLink) element).getParent();
		// }
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
		if (element instanceof Domain)
		{
			long count = 0L;
			ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle()
					.getBundleContext(), ConnectionService.class, null);
			connectionServiceTracker.open();
			try
			{
				ConnectionService con = (ConnectionService) connectionServiceTracker.getService();
				if (con != null)
				{
					Domain domain = (Domain) element;
					AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) con.getQuery(AddressGroupCategory.class);
					count = query.countByDomain(domain);
				}
			}
			finally
			{
				connectionServiceTracker.close();
			}
			return count > 0l;
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
