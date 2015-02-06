package ch.eugster.events.persistence.model;

import java.util.List;


public interface Donator
{
	public List<Donation> getDonations();

	public void addDonation(Donation donation);

	public void removeDonation(Donation donation);
}
