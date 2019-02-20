package ch.eugster.events.charity.handlers;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.charity.editors.CharityRunnerEditor;
import ch.eugster.events.charity.editors.CharityRunnerEditorInput;
import ch.eugster.events.charity.views.CharityRunView;
import ch.eugster.events.persistence.model.CharityRunner;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;


public class EditCharityRunnerHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof CharityRunView)
		{
			CharityRunView view = (CharityRunView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			if (ssel.getFirstElement() instanceof CharityRunner)
			{
				CharityRunner charityRunner = (CharityRunner) ssel.getFirstElement();
				try
				{
					if (connectionService != null)
					{
						charityRunner = (CharityRunner) connectionService.refresh(charityRunner);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new CharityRunnerEditorInput(charityRunner), CharityRunnerEditor.ID);
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
			this.setBaseEnabled(connectionService != null && ssel.getFirstElement() instanceof CharityRunner);
		}
	}
}
