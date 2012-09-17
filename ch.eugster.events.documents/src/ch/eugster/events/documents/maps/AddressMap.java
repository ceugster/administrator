package ch.eugster.events.documents.maps;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Donation;

public class AddressMap extends AbstractDataMap
{
	private static NumberFormat amountFormatter = null;

	public AddressMap(final Address address)
	{
		this(address, null);
	}

	public AddressMap(final Address address, final Integer year)
	{
		if (amountFormatter == null)
		{
			amountFormatter = DecimalFormat.getNumberInstance();
			amountFormatter.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setGroupingUsed(true);
		}

		for (Key key : Key.values())
		{
			if (year == null)
			{
				this.setProperty(key.getKey(), key.getValue(address));
			}
			else
			{
				this.setProperty(key.getKey(), key.getValue(address, year.intValue()));
			}
		}

		for (TableKey key : TableKey.values())
		{
			this.addTableMaps(key.getKey(), key.getTableMaps(address, year));
		}

	}

	public enum Key implements DataMapKey
	{
		NAME, ANOTHER_LINE, ADDRESS, POB, ZIP, CITY, COUNTRY, PHONE, FAX, SALUTATION, EMAIL, WEBSITE, POLITE, MAILING_ADDRESS, COUNTY, TOTAL_DONATIONS;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case NAME:
				{
					return "Name (Adresse)";
				}
				case ANOTHER_LINE:
				{
					return "Zusatzzeile (Adresse)";
				}
				case ADDRESS:
				{
					return "Strasse";
				}
				case POB:
				{
					return "Postfach";
				}
				case ZIP:
				{
					return "Postleitzahl";
				}
				case CITY:
				{
					return "Ort";
				}
				case COUNTRY:
				{
					return "Land";
				}
				case PHONE:
				{
					return "Telefon (Adresse)";
				}
				case FAX:
				{
					return "Fax";
				}
				case SALUTATION:
				{
					return "Anrede (Adresse)";
				}
				case EMAIL:
				{
					return "Email (Adresse)";
				}
				case WEBSITE:
				{
					return "Website (Adresse)";
				}
				case POLITE:
				{
					return "Briefanrede (Adresse)";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift (Adresse)";
				}
				case COUNTY:
				{
					return "Kanton";
				}
				case TOTAL_DONATIONS:
				{
					return "Spendenbetrag";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getKey()
		{
			switch (this)
			{
				case NAME:
				{
					return "address_name";
				}
				case ANOTHER_LINE:
				{
					return "another_line";
				}
				case ADDRESS:
				{
					return "address_address";
				}
				case POB:
				{
					return "address_pob";
				}
				case ZIP:
				{
					return "address_zip";
				}
				case CITY:
				{
					return "address_city";
				}
				case COUNTRY:
				{
					return "address_country";
				}
				case PHONE:
				{
					return "address_phone";
				}
				case FAX:
				{
					return "address_fax";
				}
				case SALUTATION:
				{
					return "salutation";
				}
				case EMAIL:
				{
					return "address_email";
				}
				case WEBSITE:
				{
					return "address_website";
				}
				case POLITE:
				{
					return "polite";
				}
				case MAILING_ADDRESS:
				{
					return "mailing_address";
				}
				case COUNTY:
				{
					return "address_county";
				}
				case TOTAL_DONATIONS:
				{
					return "total_donations";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getName()
		{
			switch (this)
			{
				case NAME:
				{
					return "Name";
				}
				case ANOTHER_LINE:
				{
					return "Zusatzzeile";
				}
				case ADDRESS:
				{
					return "Strasse";
				}
				case POB:
				{
					return "Postfach";
				}
				case ZIP:
				{
					return "Postleitzahl";
				}
				case CITY:
				{
					return "Ort";
				}
				case COUNTRY:
				{
					return "Land";
				}
				case PHONE:
				{
					return "Telefon";
				}
				case FAX:
				{
					return "Fax";
				}
				case SALUTATION:
				{
					return "Anrede";
				}
				case EMAIL:
				{
					return "Email";
				}
				case WEBSITE:
				{
					return "Website";
				}
				case POLITE:
				{
					return "Briefanrede";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift";
				}
				case COUNTY:
				{
					return "Kanton";
				}
				case TOTAL_DONATIONS:
				{
					return "Spendenbetrag";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Address address)
		{
			switch (this)
			{
				case NAME:
				{
					return address.getName();
				}
				case ANOTHER_LINE:
				{
					return address.getAnotherLine();
				}
				case ADDRESS:
				{
					return address.getAddress();
				}
				case POB:
				{
					return address.getPob();
				}
				case ZIP:
				{
					return address.getZip();
				}
				case CITY:
				{
					return address.getCity();
				}
				case COUNTRY:
				{
					return address.getCountry() == null ? "" : address.getCountry().getIso3166alpha2();
				}
				case PHONE:
				{
					return address.getPhone();
				}
				case FAX:
				{
					return address.getFax();
				}
				case SALUTATION:
				{
					AddressSalutation salutation = address.getSalutation();
					return salutation == null ? "" : salutation.getSalutation();
				}
				case EMAIL:
				{
					return address.getEmail();
				}
				case WEBSITE:
				{
					return address.getWebsite();
				}
				case POLITE:
				{
					AddressSalutation salutation = address.getSalutation();
					return salutation == null ? "" : salutation.getPolite();
				}
				case MAILING_ADDRESS:
				{
					return AddressFormatter.getInstance().formatAddressLabel(address);
				}
				case COUNTY:
				{
					return address.getProvince();
				}
				case TOTAL_DONATIONS:
				{
					double totalAmount = 0D;
					Collection<Donation> donations = address.getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							totalAmount += donation.getAmount();
						}
					}
					return amountFormatter.format(totalAmount);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Address address, final int year)
		{
			switch (this)
			{
				case NAME:
				{
					return address.getName();
				}
				case ANOTHER_LINE:
				{
					return address.getAnotherLine();
				}
				case ADDRESS:
				{
					return address.getAddress();
				}
				case POB:
				{
					return address.getPob();
				}
				case ZIP:
				{
					return address.getZip();
				}
				case CITY:
				{
					return address.getCity();
				}
				case COUNTRY:
				{
					return address.getCountry() == null ? "" : address.getCountry().getIso3166alpha2();
				}
				case PHONE:
				{
					return address.getPhone();
				}
				case FAX:
				{
					return address.getFax();
				}
				case SALUTATION:
				{
					AddressSalutation salutation = address.getSalutation();
					return salutation == null ? "" : salutation.getSalutation();
				}
				case EMAIL:
				{
					return address.getEmail();
				}
				case WEBSITE:
				{
					return address.getWebsite();
				}
				case POLITE:
				{
					AddressSalutation salutation = address.getSalutation();
					return salutation == null ? "" : salutation.getPolite();
				}
				case MAILING_ADDRESS:
				{
					return AddressFormatter.getInstance().formatAddressLabel(address);
				}
				case COUNTY:
				{
					return address.getProvince();
				}
				case TOTAL_DONATIONS:
				{
					double totalAmount = 0D;
					Collection<Donation> donations = address.getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							if (year == donation.getDonationYear())
							{
								totalAmount += donation.getAmount();
							}
						}
					}
					return amountFormatter.format(totalAmount);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

	public enum TableKey
	{
		DONATION;

		public String getDescription()
		{
			switch (this)
			{
				case DONATION:
				{
					return "Spenden";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getKey()
		{
			switch (this)
			{
				case DONATION:
				{
					return "donations";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap> getTableMaps(final Address address)
		{
			switch (this)
			{
				case DONATION:
				{
					return this.getTableMaps(address, null);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap> getTableMaps(final Address address, final Integer year)
		{
			switch (this)
			{
				case DONATION:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					Collection<Donation> donations = address.getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							if (year == null)
							{
								tableMaps.add(new DonationMap(donation));
							}
							else
							{
								int donationYear = donation.getDonationDate().get(Calendar.YEAR);
								if (donationYear == year.intValue())
								{
									tableMaps.add(new DonationMap(donation));
								}
							}
						}
					}
					return tableMaps;
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}
}
