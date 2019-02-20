package ch.eugster.events.person.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.queries.BankAccountQuery;
import ch.eugster.events.person.views.PersonBankAccountView;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteBankAccountHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) context.getParent().getVariable("activeShell");
		if (context.getParent().getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty())
			{
				if (ssel.getFirstElement() instanceof BankAccount)
				{
					if (MessageDialog.openQuestion(shell, "Bankverbindung entfernen", "Soll die ausgewählte Bankverbindung entfernt werden?"))
					{
						BankAccount account = (BankAccount) ssel.getFirstElement();
						BankAccountQuery deleteQuery = (BankAccountQuery) connectionService
													.getQuery(BankAccount.class);
						Iterator<?> iterator = ssel.iterator();
						while (iterator.hasNext())
						{
							Object toBeDeleted = iterator.next();
							if (toBeDeleted instanceof BankAccount)
							{
								deleteQuery.delete(account);
							}
						}
					}
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
				if (ssel.getFirstElement() instanceof BankAccount)
				{
					enabled = true;
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
