package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.visits.editors.SchoolLevelEditor;
import ch.eugster.events.visits.editors.SchoolLevelEditorInput;

public class EditSchoolLevelHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			Object selection = context.getParent().getVariable("selection");
			if (selection instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) selection;
				Object element = ssel.getFirstElement();
				if (element instanceof SchoolLevel)
				{
					SchoolLevel schoolLevel = (SchoolLevel) element;
					try
					{
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.openEditor(new SchoolLevelEditorInput(schoolLevel), SchoolLevelEditor.ID, true);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

}
