package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.nebula.widgets.ganttchart.ISettings;
import org.eclipse.ui.IWorkbenchPart;

import ch.eugster.events.visits.views.OverviewView;

public class SetYearViewHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			IWorkbenchPart activePart = (IWorkbenchPart) context.getParent().getVariable("activePart");
			if (activePart instanceof OverviewView)
			{
				OverviewView view = (OverviewView) activePart;
				view.setLevel(11, ISettings.VIEW_YEAR);
			}
		}
		return null;
	}

}
