package ch.eugster.events.todo.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;

import ch.eugster.events.todo.views.TodoView;

public class ReloadTodoListHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof TodoView)
		{
			TodoView view = (TodoView) context.getVariable("activePart");
			view.reloadTodoList();
		}
		return Status.OK_STATUS;
	}

}
