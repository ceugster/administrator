package ch.eugster.events.category.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.category.editors.CategoryEditor;
import ch.eugster.events.category.editors.CategoryEditorInput;
import ch.eugster.events.persistence.model.Category;

public class EditCategoryHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
			if (!ssel.isEmpty() && ssel.size() == 1)
			{
				if (ssel.getFirstElement() instanceof Category)
				{
					Category category = (Category) ssel.getFirstElement();
					CategoryEditorInput input = new CategoryEditorInput(category);
					try
					{
						IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
						window.getActivePage().openEditor(input, CategoryEditor.ID);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

}
