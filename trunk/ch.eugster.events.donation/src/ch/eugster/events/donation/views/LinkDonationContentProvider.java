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
			System.out.println("retrieving donations from person " + person.getId());
			Collection<Donation> d = person.getDonations();
			donations = d.toArray(new Donation[0]);
		}
		else if (inputElement instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) inputElement;
			System.out.println("retrieving donations from link " + link.getId());
			donations = link.getDonations().toArray(new Donation[0]);
		}
		else if (inputElement instanceof Address)
		{
			Address address = (Address) inputElement;
			System.out.println("retrieving donations from address " + address.getId());
			donations = address.getDonations().toArray(new Donation[0]);
		}
		System.out.println("return " + donations.length + " donations");
		return donations;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
