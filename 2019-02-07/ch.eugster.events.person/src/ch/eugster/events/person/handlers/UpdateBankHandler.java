package ch.eugster.events.person.handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.person.dialogs.UpdateBankDialog;


public class UpdateBankHandler extends AbstractHandler implements IHandler 
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) context.getParent().getVariable("activeShell");
		UpdateBankDialog dialog = new UpdateBankDialog(shell);
		dialog.open();
		return Status.OK_STATUS;
	}

}
