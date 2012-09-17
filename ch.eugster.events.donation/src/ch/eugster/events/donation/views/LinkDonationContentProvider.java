package ch.eugster.events.donation.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class LinkDonationContentProvider implements IStructuredContentProvider
{
	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof Person)
		{
			return ((Person) inputElement).getDonations().toArray(new Donation[0]);
		}
		else if (inputElement instanceof LinkPersonAddress)
		{
			return ((LinkPersonAddress) inputElement).getDonations().toArray(new Donation[0]);
		}
		else if (inputElement instanceof Address)
		{
			return ((Address) inputElement).getDonations().toArray(new Donation[0]);
		}
		return new Donation[0];
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}
}
