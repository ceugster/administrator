package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class RefreshHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker tracker;

	@Override
	public void dispose()
	{
		tracker.close();
		super.dispose();
	}

	public RefreshHandler()
	{
		tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			Object object = context.getVariable("selection");
			if (object instanceof IStructuredSelection)
			{
				AddressGroupCategory category = null;
				AddressGroup group = null;
				IStructuredSelection ssel = (IStructuredSelection) object;
				if (ssel.getFirstElement() instanceof AddressGroupCategory)
				{
					category = (AddressGroupCategory) ssel.getFirstElement();
					category = refresh(category);
				}
				else if (ssel.getFirstElement() instanceof AddressGroup)
				{
					group = (AddressGroup) ssel.getFirstElement();
					group = refresh(group);
				}

				object = context.getVariable("activePart");
				if (object instanceof AddressGroupView)
				{
					AddressGroupView view = (AddressGroupView) object;
					if (category != null)
					{
						view.getViewer().refresh(category);
						view.getViewer().setSelection(new StructuredSelection(new Object[] { category }));
					}
					else if (group != null)
					{
						view.getViewer().refresh(group);
						view.getViewer().setSelection(new StructuredSelection(new Object[] { group }));
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private AddressGroupCategory refresh(AddressGroupCategory category)
	{
		ConnectionService service = (ConnectionService) tracker.getService();
		try
		{
			AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) service.getQuery(AddressGroupCategory.class);
			return (AddressGroupCategory) query.refresh(category);
		}
		catch (Exception e)
		{
			AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) service.getQuery(AddressGroupCategory.class);
			return query.find(AddressGroupCategory.class, category.getId());
		}
	}

	private AddressGroup refresh(AddressGroup addressGroup)
	{
		ConnectionService service = (ConnectionService) tracker.getService();
		try
		{
			AddressGroupQuery query = (AddressGroupQuery) service.getQuery(AddressGroup.class);
			return (AddressGroup) query.refresh(addressGroup);
		}
		catch (Exception e)
		{
			AddressGroupQuery query = (AddressGroupQuery) service.getQuery(AddressGroup.class);
			return query.find(AddressGroup.class, addressGroup.getId());
		}
	}

}
