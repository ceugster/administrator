package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;

import ch.eugster.events.visits.views.ThemeView;

public class SetTodayCentered extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			IWorkbenchPart activePart = (IWorkbenchPart) context.getParent().getVariable("activePart");
			if (activePart instanceof ThemeView)
			{
				ThemeView view = (ThemeView) activePart;
				view.setTodayCentered();
			}
		}
		return Status.OK_STATUS;
	}

}
