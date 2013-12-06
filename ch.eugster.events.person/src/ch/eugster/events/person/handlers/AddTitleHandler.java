package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.person.editors.PersonTitleEditor;
import ch.eugster.events.person.editors.PersonTitleEditorInput;

public class AddTitleHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		try
		{
			IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
			window.getActivePage().openEditor(new PersonTitleEditorInput(PersonTitle.newInstance()), PersonTitleEditor.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
