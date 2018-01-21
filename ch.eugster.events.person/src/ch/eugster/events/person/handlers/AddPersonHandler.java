package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.person.dialogs.SelectEditorInputTypeDialog;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class AddPersonHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		SelectEditorInputTypeDialog dialog = new SelectEditorInputTypeDialog((Shell) context.getVariable("activeShell"), connectionService);
		dialog.open();
		return Status.OK_STATUS;
	}
}
