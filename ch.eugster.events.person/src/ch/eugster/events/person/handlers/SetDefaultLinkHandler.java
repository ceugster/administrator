package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class SetDefaultLinkHandler extends ConnectionServiceDependentAbstractHandler
{
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
						if (connectionService != null)
						{
							LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
							link.getPerson().setDefaultLink(link);
							LinkPersonAddressQuery query = (LinkPersonAddressQuery) connectionService.getQuery(LinkPersonAddress.class);
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
		this.setBaseEnabled(connectionService != null);
	}
}
