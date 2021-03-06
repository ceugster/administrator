package ch.eugster.events.season.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.SeasonQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class ToggleActiveSeasonHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
			if (!ssel.isEmpty() && ssel.size() == 1)
			{
				if (ssel.getFirstElement() instanceof Season)
				{
					Season season = (Season) ssel.getFirstElement();
					season.setClosed(!season.isClosed());

					if (connectionService != null)
					{
						SeasonQuery query = (SeasonQuery) connectionService.getQuery(Season.class);
						season = query.merge(season);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext)
	{
		EvaluationContext ctx = (EvaluationContext) evaluationContext;
		ISelection sel = (ISelection) ctx.getVariable("selection");
		if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) sel;
			if (ssel.getFirstElement() instanceof Season)
			{
				Season season = (Season) ssel.getFirstElement();
				this.setBaseEnabled(!season.isClosed());
			}
		}
	}

}
