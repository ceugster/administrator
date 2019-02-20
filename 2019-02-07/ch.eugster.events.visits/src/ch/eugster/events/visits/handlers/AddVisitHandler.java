package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public class AddVisitHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			Visit visit = Visit.newInstance();
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new VisitEditorInput(visit), VisitEditor.ID, true);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		return Status.OK_STATUS;
	}

}
