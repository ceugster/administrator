package ch.eugster.events.charity.handlers;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.charity.editors.CharityRunEditor;
import ch.eugster.events.charity.editors.CharityRunEditorInput;
import ch.eugster.events.charity.views.CharityRunView;
import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;


public class EditCharityRunHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof CharityRunView)
		{
			CharityRunView view = (CharityRunView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CharityRun)
			{
				CharityRun charityRun = (CharityRun) ssel.getFirstElement();
				try
				{
					if (connectionService != null)
					{
						charityRun = (CharityRun) connectionService.refresh(charityRun);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new CharityRunEditorInput(charityRun), CharityRunEditor.ID);
					}
				} 
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("activePart") instanceof CharityRunView)
		{
			CharityRunView view = (CharityRunView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			this.setBaseEnabled(connectionService != null && ssel.getFirstElement() instanceof CharityRun);
		}
	}
}
