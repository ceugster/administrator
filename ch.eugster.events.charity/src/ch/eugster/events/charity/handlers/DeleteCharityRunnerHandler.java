package ch.eugster.events.charity.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.charity.views.CharityRunView;
import ch.eugster.events.persistence.model.CharityRunner;
import ch.eugster.events.persistence.queries.CharityRunnerQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;


public class DeleteCharityRunnerHandler extends ConnectionServiceDependentAbstractHandler
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
				if (connectionService != null)
				{
					if (MessageDialog.openQuestion(view.getSite().getShell(), "Sponsorlauf entfernen", "Soll der gewählte Sponsorlauf entfernt werden?"))
					{
						CharityRunnerQuery query = (CharityRunnerQuery) connectionService.getQuery(CharityRunner.class);
						charityRunner = query.delete(charityRunner);
					}
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
			this.setBaseEnabled(connectionService != null && ssel.getFirstElement() instanceof CharityRunner);
		}
	}
}
