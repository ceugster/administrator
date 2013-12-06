package ch.eugster.events.donation.views;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class LinkDonationContentProvider implements IStructuredContentProvider
{
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object inputElement)
	{
		Donation[] donations = new Donation[0];
		if (inputElement instanceof Person)
		{
			Person person = (Person) inputElement;
			Collection<Donation> d = person.getDefaultLink().getDonations();
			donations = d.toArray(new Donation[0]);
		}
		else if (inputElement instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) inputElement;
			donations = link.getDonations().toArray(new Donation[0]);
		}
		else if (inputElement instanceof Address)
		{
			Address address = (Address) inputElement;
			donations = address.getDonations().toArray(new Donation[0]);
		}
		return donations;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
