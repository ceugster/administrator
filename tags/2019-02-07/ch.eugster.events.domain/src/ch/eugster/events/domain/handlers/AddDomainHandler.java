package ch.eugster.events.domain.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.domain.editors.DomainEditor;
import ch.eugster.events.domain.editors.DomainEditorInput;
import ch.eugster.events.persistence.model.Domain;

public class AddDomainHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Domain domain = Domain.newInstance();
		DomainEditorInput input = new DomainEditorInput(domain);
		try
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
				window.getActivePage().openEditor(input, DomainEditor.ID);
			}
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
