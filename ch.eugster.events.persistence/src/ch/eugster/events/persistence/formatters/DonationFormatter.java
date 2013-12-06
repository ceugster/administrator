package ch.eugster.events.persistence.formatters;

import ch.eugster.events.persistence.model.Donation;

public class DonationFormatter
{
	private static DonationFormatter instance;

	public static DonationFormatter getInstance()
	{
		if (instance == null)
		{
			instance = new DonationFormatter();
		}
		return instance;
	}

	public String formatDonatorName(Donation donation)
	{
		if (donation.getLink() != null)
		{
			return PersonFormatter.getInstance().formatLastnameFirstname(donation.getLink().getPerson());
		}
		if (donation.getAddress() != null)
		{
			return donation.getAddress().getName();
		}
		return "";
	}

	public String formatDonatorAddress(Donation donation)
	{
		if (donation.getAddress() != null)
		{
			return AddressFormatter.getInstance().formatAddressLine(donation.getAddress());
		}
		return "";
	}

	// public String formatDonatorCityLine(Country country, Donation donation)
	// {
	// if (donation.getLink() != null)
	// {
	// return
	// AddressFormatter.getInstance().formatCityLine(donation.getLink().getAddress());
	// }
	// if (donation.getAddress() != null)
	// {
	// return
	// AddressFormatter.getInstance().formatCityLine(donation.getAddress());
	// }
	// return "";
	// }

	public String formatDonatorCityLine(Donation donation)
	{
		if (donation.getAddress() != null)
		{
			return AddressFormatter.getInstance().formatCityLine(donation.getAddress());
		}
		return "";
	}

}
