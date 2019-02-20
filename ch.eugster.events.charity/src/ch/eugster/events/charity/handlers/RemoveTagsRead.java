package ch.eugster.events.charity.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.persistence.model.CharityRunTagRead;
import ch.eugster.events.persistence.queries.CharityRunTagReadQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class RemoveTagsRead extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Object variable = context.getVariable("selection");
		if (variable instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) variable;
			if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CharityRun)
			{
				CharityRun charityRun = (CharityRun) ssel.getFirstElement();
				Shell shell = (Shell) context.getVariable("activeShell");
				if (MessageDialog.openQuestion(shell, "Z�hlungen l�schen", "Sollen die Z�hlungen des Sponsorlaufs " + charityRun.getName() + " gel�scht werden?"))
				{
					CharityRunTagReadQuery query = (CharityRunTagReadQuery) this.connectionService.getQuery(CharityRunTagRead.class);
					int result = query.deleteByCharityRun(charityRun);
					MessageDialog.openConfirm(shell, "Z�hlungen gel�scht", "Es wurden " + result + " Z�hlungen gel�scht.");
				}
				
			}
		}
		return Status.OK_STATUS;
	}

}
