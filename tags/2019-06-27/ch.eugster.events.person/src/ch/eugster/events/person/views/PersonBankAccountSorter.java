package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.BankAccount;

public class PersonBankAccountSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		BankAccount ba1 = (BankAccount) e1;
		BankAccount ba2 = (BankAccount) e2;

		return ba1.getIban().compareTo(ba2.getIban());
	}

}
