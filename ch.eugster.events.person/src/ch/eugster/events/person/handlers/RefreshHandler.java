package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;

import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class RefreshHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			Object part = context.getVariable("activePart");
			if (part instanceof PersonView)
			{
				PersonView view = (PersonView) part;
				view.getSearcher().schedule();
			}
		}
		return Status.OK_STATUS;
	}

}
