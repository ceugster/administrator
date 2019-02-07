package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class PersonBankAccountContentProvider implements IStructuredContentProvider
{
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object inputElement)
	{
		BankAccount[] accounts = new BankAccount[0];
		if (inputElement instanceof Person)
		{
			Person person = (Person) inputElement;
			accounts = person.getBankAccounts().toArray(new BankAccount[0]);
		}
		else if (inputElement instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) inputElement;
			accounts = link.getPerson().getBankAccounts().toArray(new BankAccount[0]);
		}
		else if (inputElement instanceof Address)
		{
			Address address = (Address) inputElement;
			accounts = address.getBankAccounts().toArray(new BankAccount[0]);
		}
		return accounts;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
