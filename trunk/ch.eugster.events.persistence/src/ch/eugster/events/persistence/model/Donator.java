package ch.eugster.events.persistence.model;

import java.util.Collection;

public interface Donator
{
	public Collection<Donation> getDonations();

	public void addDonation(Donation donation);

	public void removeDonation(Donation donation);
}
