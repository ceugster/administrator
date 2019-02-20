package ch.eugster.events.addressgroup.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.addressgroup.views.AddressGroupMemberView;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteAddressGroupMemberHandler extends ConnectionServiceDependentAbstractHandler
{
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
