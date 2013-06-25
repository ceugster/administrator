package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.service.ConnectionService;

public class RefreshHandler extends AbstractHandler implements IHandler
{

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
				}
				else if (ssel.getFirstElement() instanceof AddressGroup)
				{
					group = (AddressGroup) ssel.getFirstElement();
				}
				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ConnectionService.class.getName(), null);
				tracker.open();
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					object = context.getVariable("activePart");
					if (object instanceof AddressGroupView)
					{
						AddressGroupView view = (AddressGroupView) object;
						if (category != null)
						{
							view.getViewer().refresh(service.refresh(category));
						}
						else if (group != null)
						{
							view.getViewer().refresh(service.refresh(group));
						}
					}
				}
			}
		}
		return null;
	}

}
