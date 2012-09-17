package ch.eugster.events.merge.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.person.editors.PersonEditorInput;
import ch.eugster.events.person.editors.PersonFormEditor;

public class EditPersonHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
					try
					{
						IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
						window.getActivePage().openEditor(new PersonEditorInput(link), PersonFormEditor.ID);
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

	@Override
	public boolean isEnabled()
	{
		return super.isEnabled();
	}

	@Override
	public void setEnabled(final Object object)
	{
		if (object instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) object;
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				setBaseEnabled(ssel.size() == 1);
			}
		}
	}
}
