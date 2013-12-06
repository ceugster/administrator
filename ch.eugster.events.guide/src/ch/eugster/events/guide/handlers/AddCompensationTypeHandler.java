package ch.eugster.events.guide.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.guide.editors.CompensationTypeEditor;
import ch.eugster.events.guide.editors.CompensationTypeEditorInput;
import ch.eugster.events.persistence.model.CompensationType;

public class AddCompensationTypeHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		CompensationType type = CompensationType.newInstance();
		CompensationTypeEditorInput input = new CompensationTypeEditorInput(type);
		try
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
				window.getActivePage().openEditor(input, CompensationTypeEditor.ID);
			}
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
