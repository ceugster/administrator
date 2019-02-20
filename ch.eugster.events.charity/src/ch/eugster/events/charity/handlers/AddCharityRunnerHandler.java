package ch.eugster.events.charity.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.charity.editors.CharityRunnerEditor;
import ch.eugster.events.charity.editors.CharityRunnerEditorInput;
import ch.eugster.events.charity.views.CharityRunView;
import ch.eugster.events.persistence.model.CharityPerson;
import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.persistence.model.CharityRunner;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class AddCharityRunnerHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof CharityRunView)
		{
			CharityRunView view = (CharityRunView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			CharityRunner newRunner = null;
			if (ssel.getFirstElement() instanceof CharityRun)
			{
				newRunner = CharityRunner.newInstance((CharityRun) ssel.getFirstElement(), CharityPerson.newInstance());
			}
			else if (ssel.getFirstElement() instanceof CharityRunner)
			{
				CharityRunner runner = (CharityRunner) ssel.getFirstElement();
				if (runner.hasLeadership())
				{
					newRunner = CharityRunner.newInstance(runner.getCharityRun(), CharityPerson.newInstance(), runner);
				}
			}
			if (newRunner != null)
			{
				try
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new CharityRunnerEditorInput(newRunner), CharityRunnerEditor.ID);
				} 
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
		return IStatus.OK;
	}

	@Override
	public void setEnabled(Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("activePart") instanceof CharityRunView)
		{
			CharityRunView view = (CharityRunView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			boolean enabled = false;
			if (ssel.getFirstElement() instanceof CharityRun)
			{
				enabled = true;
			}
			else if (ssel.getFirstElement() instanceof CharityRunner)
			{
				CharityRunner runner = (CharityRunner) ssel.getFirstElement();
				enabled = runner.hasLeadership();
			}
			this.setBaseEnabled(connectionService != null && enabled);
		}
	}
}
