package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.dialogs.BankAccountDialog;
import ch.eugster.events.person.views.PersonBankAccountView;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class AddBankAccountHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) context.getParent().getVariable("activeShell");
		Object part = context.getVariable("activePart");
		if (part instanceof PersonBankAccountView)
		{
			PersonBankAccountView view = (PersonBankAccountView) part;
			Object input = view.getViewer().getInput();
			if (input instanceof Person)
			{
				Person person = (Person) input;
				BankAccount account = BankAccount.newInstance(person);
				BankAccountDialog dialog = new BankAccountDialog(shell, account);
				dialog.open();
			}
			else if (input instanceof Address)
			{
				Address address = (Address) input;
				BankAccount account = BankAccount.newInstance(address);
				BankAccountDialog dialog = new BankAccountDialog(shell, account);
				dialog.open();
			}
		}
		
		return Status.OK_STATUS;
	}
}
