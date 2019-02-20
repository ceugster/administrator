package ch.eugster.events.user.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.model.User;
import ch.eugster.events.user.editors.UserEditor;
import ch.eugster.events.user.editors.UserEditorInput;

public class AddUserHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		User user = User.newInstance();
		UserEditorInput input = new UserEditorInput(user);
		try
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
				window.getActivePage().openEditor(input, UserEditor.ID);
			}
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
