package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.person.dialogs.BankAccountDialog;
import ch.eugster.events.person.views.PersonBankAccountView;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class EditBankAccountHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			Shell shell = (Shell) context.getParent().getVariable("activeShell");
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				if (ssel.getFirstElement() instanceof BankAccount)
				{
					BankAccount account = (BankAccount) ssel.getFirstElement();
					BankAccountDialog dialog = new BankAccountDialog(shell, account);
					dialog.open();
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) 
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context instanceof EvaluationContext)
		{
			Object object = context.getVariable("activePart");
			if (object instanceof PersonBankAccountView)
			{
				PersonBankAccountView view = (PersonBankAccountView) object;
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (ssel.size() == 1 && ssel.getFirstElement() instanceof BankAccount)
				{
					enabled = true;
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
