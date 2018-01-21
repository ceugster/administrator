package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.Member;

public class AddressMap extends AbstractDataMap<Address>
{
	private static NumberFormat amountFormatter = null;

	protected AddressMap() {
		super();
	}

	public AddressMap(final Address address)
	{
		this(address, null, null, null, false);
	}

	public AddressMap(final Address address, boolean isGroup)
	{
		this(address, null, null, null, isGroup);
	}

	public AddressMap(final Address address, final Integer year, DonationPurpose purpose, Domain domain, boolean isGroup)
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
				this.setProperty(key.getKey(), key.getValue(address, isGroup));
			}
			else
			{
				this.setProperty(key.getKey(), key.getValue(address, year.intValue(), isGroup));
			}
		}

		for (TableKey key : TableKey.values())
		{
			this.addTableMaps(key.getKey(), key.getTableMaps(address, year, purpose, domain));
		}

	}

	protected void printTables(Writer writer)
	{
		printHeader(writer, 2, "Tabellen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, null, TableKey.DONATIONS.getKey());
		printCell(writer, "#donation", "Spenden");
		endTableRow(writer);
		endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		NAME, ANOTHER_LINE, ADDRESS, POB, ZIP, CITY, COUNTRY, PHONE, FAX, SALUTATION, EMAIL, WEBSITE, POLITE, MAILING_ADDRESS, COUNTY, TOTAL_DONATIONS, MEMBER, NOTES;

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
				case MEMBER:
				{
					return "Mitglied";
				}
				case NOTES:
				{
					return "Bemerkungen Adresse";
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
				case MEMBER:
				{
					return "member";
				}
				case NOTES:
				{
					return "notes";
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
					return "Organisation";
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
				case MEMBER:
				{
					return "Mitglied";
				}
				case NOTES:
				{
					return "Bemerkungen Adresse";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Address address, boolean isGroup)
		{
			switch (this)
			{
				case NAME:
				{
					if (isGroup)
					{
						return address.getName();
					}
					else
					{
						if (address.getSalutation() == null)
						{
							return address.getName();
						}
						else
						{
							return address.getSalutation().isShowAddressNameForPersons() ? address.getName() : "";
						}
					}
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
					return address.getZipCode() == null ? address.getZip() : address.getZipCode().getZip();
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
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(address.getCountry(),
							address.getPhone());
				}
				case FAX:
				{
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(address.getCountry(),
							address.getFax());
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
					List<Donation> donations = address.getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							totalAmount += donation.getAmount();
						}
					}
					return amountFormatter.format(totalAmount);
				}
				case MEMBER:
				{
					StringBuilder builder = new StringBuilder();
					Member[] members = address.getMembers().toArray(new Member[0]);
					for (int i = 0; i < members.length; i++)
					{
						builder = builder.append(members[i].getMembership().getName());
						if (i < members.length - 1)
						{
							builder = builder.append(", ");
						}
					}
					return builder.toString();
				}
				case NOTES:
				{
					return address.getNotes();
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Address address, final int year, boolean isGroup)
		{
			switch (this)
			{
				case NAME:
				{
					return isGroup ? address.getName() : (address.getSalutation().isShowAddressNameForPersons() ? address.getName() : "");
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
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(address.getCountry(),
							address.getPhone());
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
					List<Donation> donations = address.getDonations();
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
				case MEMBER:
				{
					StringBuilder builder = new StringBuilder();
					Member[] members = address.getMembers().toArray(new Member[0]);
					for (int i = 0; i < members.length; i++)
					{
						builder = builder.append(members[i].getMembership().getName());
						if (i < members.length - 1)
						{
							builder = builder.append(", ");
						}
					}
					return builder.toString();
				}
				case NOTES:
				{
					return address.getNotes();
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
		DONATIONS;

		public String getDescription()
		{
			switch (this)
			{
				case DONATIONS:
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
				case DONATIONS:
				{
					return "table_donations";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap<?>> getTableMaps(final Address address)
		{
			switch (this)
			{
				case DONATIONS:
				{
					return this.getTableMaps(address, null, null, null);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap<?>> getTableMaps(final Address address, final Integer year, DonationPurpose purpose,
				Domain domain)
		{
			switch (this)
			{
				case DONATIONS:
				{
					List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
					List<Donation> donations = address.getDonations();
					for (Donation donation : donations)
					{
						addDonation(donation, tableMaps, year, purpose, domain);
					}
					return tableMaps;
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		private void addDonation(Donation donation, List<DataMap<?>> tableMaps, Integer year, DonationPurpose purpose,
				Domain domain)
		{
			if (printDonation(donation, purpose, domain))
			{
				if (year == null)
				{
					tableMaps.add(new DonationMap(donation));
				}
				else
				{
					if (donation.getDonationYear() == year.intValue())
					{
						tableMaps.add(new DonationMap(donation));
					}
				}
			}
		}

		private boolean printDonation(Donation donation, DonationPurpose purpose, Domain domain)
		{
			if (donation.isDeleted())
			{
				return false;
			}
			if (purpose == null && domain == null)
			{
				return true;
			}

			if (purpose == null)
			{
				if (domain.getId() != null)
				{
					if (donation.getDomain() == null)
					{
						return false;
					}
					else
					{
						return domain.getId().equals(donation.getDomain().getId());
					}
				}
			}
			if (domain == null)
			{
				return donation.getPurpose().getId().equals(purpose.getId());
			}
			if (donation.getDomain() == null || donation.getPurpose() == null)
			{
				return false;
			}
			if (donation.getDomain() != null && domain.getId().equals(donation.getDomain().getId()))
			{
				if (donation.getPurpose().getId().equals(purpose.getId()))
				{
					return true;
				}
			}
			return false;
		}

	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}

	
}
