package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;

import ch.eugster.events.person.views.PersonView;

public class ClearSearchFields extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof PersonView)
		{
			PersonView view = (PersonView) context.getVariable("activePart");
			view.clearSearchFields();
		}
		return null;
	}

}
