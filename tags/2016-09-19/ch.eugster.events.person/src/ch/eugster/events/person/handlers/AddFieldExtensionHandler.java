package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.person.editors.FieldExtensionEditor;
import ch.eugster.events.person.editors.FieldExtensionEditorInput;

public class AddFieldExtensionHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		try
		{
			IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
			window.getActivePage().openEditor(new FieldExtensionEditorInput(FieldExtension.newInstance()),
					FieldExtensionEditor.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
