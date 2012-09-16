package ch.eugster.events.addressgroup.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.views.AddressGroupMemberView;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteAddressGroupMemberHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteAddressGroupMemberHandler()
	{
		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null);
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				super.removedService(reference, service);
				setBaseEnabled(false);
			}
		};
		connectionServiceTracker.open();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("activePart") instanceof AddressGroupMemberView)
			{
				AddressGroupMemberView view = (AddressGroupMemberView) context.getParent().getVariable("activePart");
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (!ssel.isEmpty())
				{
					Iterator<?> iterator = ssel.iterator();
					while (iterator.hasNext())
					{
						Object element = iterator.next();
						if (element instanceof AbstractEntity)
						{
							AbstractEntity entity = (AbstractEntity) element;
							entity.setDeleted(true);
						}
					}
					if (connectionService != null)
					{
						if (view.getViewer().getInput() instanceof AddressGroup)
						{
							AddressGroupQuery query = (AddressGroupQuery) connectionService
									.getQuery(AddressGroup.class);
							AddressGroup addressGroup = (AddressGroup) view.getViewer().getInput();
							query.merge(addressGroup);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
			setBaseEnabled(!ssel.isEmpty());
		}
		else
		{
			setBaseEnabled(false);
		}
	}
}
