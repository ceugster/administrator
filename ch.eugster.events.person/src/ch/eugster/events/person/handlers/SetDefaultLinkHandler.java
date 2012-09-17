package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class SetDefaultLinkHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker tracker;
	
	public SetDefaultLinkHandler()
	{
		tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class.getName(), null);
		tracker.open();
	}
	
	@Override
	public void dispose() 
	{
		tracker.close();
		super.dispose();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				if (ssel.size() == 1)
				{
					if (ssel.getFirstElement() instanceof LinkPersonAddress)
					{
						ConnectionService service = (ConnectionService) tracker.getService();
						if (service != null)
						{
							LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
							link.getPerson().setDefaultLink(link);
							LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
							link = query.merge(link);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) 
	{
		this.setBaseEnabled(tracker.getService() != null);
	}
}
