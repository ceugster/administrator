package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchPart;

import ch.eugster.events.visits.views.OverviewView;

public class ZoomOutHandler extends AbstractHandler implements IHandler
{
	private static final double PPS_ADD = 240D;

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
				view.zoomOut();
			}
		}
		return null;
	}

}
