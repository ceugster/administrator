package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.EmailAccount;

public class EmailAccountSorter extends ViewerSorter
{

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		EmailAccount a1 = (EmailAccount) e1;
		EmailAccount a2 = (EmailAccount) e2;

		return a1.getUsername().compareTo(a2.getUsername());
	}
}
