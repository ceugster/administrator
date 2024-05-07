package ch.eugster.events.documents.maps;

import java.util.Calendar;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class DonationMap extends AbstractDataMap<Donation>
{
	private double amount;
	
	protected DonationMap() {
		super();
	}

	public DonationMap(final Donation donation)
	{
		this.amount = donation.getAmount();
		for (final Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(donation));
		}
	}
	
	public double getAmount()
	{
		return this.amount;
	}

	public void addAmount(double amount)
	{
		this.amount += amount;
	}

	public enum Key implements DataMapKey
	{
		TYPE, ID, DATE, AMOUNT, PURPOSE_CODE, PURPOSE_NAME, PURPOSE_DESCRIPTION, YEAR, ANOTHER_LINE, SALUTATION, POLITE, MAILING_ADDRESS;;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case TYPE:
				{
					return "Typ";
				}
				case ID:
				{
					return "Identifikation";
				}
				case DATE:
				{
					return "Spendendatum";
				}
				case AMOUNT:
				{
					return "Spendenbetrag";
				}
				case PURPOSE_CODE:
				{
					return "Zweck (Code)";
				}
				case PURPOSE_NAME:
				{
					return "Zweck (Bezeichnung)";
				}
				case PURPOSE_DESCRIPTION:
				{
					return "Zweck (Beschreibung)";
				}
				case YEAR:
				{
					return "Spendenjahr";
				}
				case ANOTHER_LINE:
				{
					return "Zusatzzeile";
				}
				case SALUTATION:
				{
					return "Anrede";
				}
				case POLITE:
				{
					return "Briefanrede";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift";
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
				case ID:
				{
					return "donator_id";
				}
				case TYPE:
				{
					return "donator_type";
				}
				case DATE:
				{
					return "donation_date";
				}
				case AMOUNT:
				{
					return "donation_amount";
				}
				case PURPOSE_CODE:
				{
					return "donation_purpose_code";
				}
				case PURPOSE_NAME:
				{
					return "donation_purpose_name";
				}
				case PURPOSE_DESCRIPTION:
				{
					return "donation_purpose_description";
				}
				case YEAR:
				{
					return "donation_year";
				}
				case ANOTHER_LINE:
				{
					return "donation_another_line";
				}
				case SALUTATION:
				{
					return "donation_salutation";
				}
				case POLITE:
				{
					return "donation_polite";
				}
				case MAILING_ADDRESS:
				{
					return "donation_mailing_address";
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
				case ID:
				{
					return "Id";
				}
				case TYPE:
				{
					return "Typ";
				}
				case DATE:
				{
					return "Datum";
				}
				case AMOUNT:
				{
					return "Betrag";
				}
				case PURPOSE_CODE:
				{
					return "Zweck";
				}
				case PURPOSE_NAME:
				{
					return "Zweck";
				}
				case PURPOSE_DESCRIPTION:
				{
					return "Zweck";
				}
				case YEAR:
				{
					return "Jahr";
				}
				case ANOTHER_LINE:
				{
					return "Zusatzzeile";
				}
				case SALUTATION:
				{
					return "Anrede";
				}
				case POLITE:
				{
					return "Briefanrede";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Donation donation)
		{
			switch (this)
			{
				case ID:
				{
					if (donation.getLink() == null)
					{
						if (donation.getAddress().getValidLinks().size() == 1)
						{
							return donation.getAddress().getValidLinks().iterator().next().getId().toString();
						}
						else
						{
							return donation.getAddress().getId().toString();
						}
					}
					else
					{
						if (donation.getLink().isDeleted() || donation.getLink().getPerson().isDeleted())
						{
							return donation.getAddress().getId().toString();
						}
						else
						{
							return donation.getLink().getAddress().getId().toString();
						}
					}
				}
				case TYPE:
				{
					if (donation.getLink() == null)
					{
						if (donation.getAddress().getValidLinks().size() == 1)
						{
							return "P";
						}
						else
						{
							return "A";
						}
					}
					else
					{
						if (donation.getLink().isDeleted() || donation.getLink().getPerson().isDeleted())
						{
							return "A";
						}
						else
						{
							return "P";
						}
					}
				}
				case DATE:
				{
					if (donation.getDonationDate() == null)
					{
						return "";
					}
					return AbstractDataMap.getDateFormatter().format(donation.getDonationDate().getTime());
				}
				case AMOUNT:
				{
					return AbstractDataMap.getAmountFormatter().format(donation.getAmount());
				}
				case PURPOSE_CODE:
				{
					return donation.getPurpose() == null ? "" : donation.getPurpose().getCode();
				}
				case PURPOSE_NAME:
				{
					return donation.getPurpose() == null ? "" : donation.getPurpose().getName();
				}
				case PURPOSE_DESCRIPTION:
				{
					return donation.getPurpose() == null ? "" : donation.getPurpose().getDescription();
				}
				case YEAR:
				{
					return AbstractDataMap.getIntegerFormatter().format(donation.getYear());
				}
				case ANOTHER_LINE:
				{
					String anotherLine = "";
					if (donation.getLink() == null)
					{
						if (donation.getAddress().getValidLinks().size() == 1)
						{
							anotherLine = donation.getAddress().getValidLinks().iterator().next().getAddress().getAnotherLine();
						}
						else
						{
							anotherLine = donation.getAddress().getAnotherLine();
						}
					}
					else
					{
						if (donation.getLink().isDeleted() || donation.getLink().getPerson().isDeleted())
						{
							anotherLine = donation.getAddress().getAnotherLine();
						}
						else
						{
							anotherLine = donation.getLink().getAddress().getAnotherLine();
						}
					}
					return anotherLine;
				}
				case SALUTATION:
				{
					if (donation.getLink() == null)
					{
						if (donation.getAddress().getValidLinks().size() == 1)
						{
							final Person person = donation.getAddress().getValidLinks().iterator().next().getPerson();
							return person.getSex() == null ? "Fehler!" : person.getSex().getSalutation();
						}
						else
						{
							return donation.getAddress().getSalutation() == null ? "" : donation.getAddress().getSalutation()
									.getSalutation();
						}
					}
					else
					{
						if (donation.getLink().isDeleted() || donation.getLink().getPerson().isDeleted())
						{
							return donation.getAddress().getSalutation() == null ? "" : donation.getAddress().getSalutation()
								.getSalutation();
						}
						else
						{
							final Person person = donation.getLink().getPerson();
							return person.getSex() == null ? "Fehler!" : person.getSex().getSalutation();
						}
					}
				}
				case POLITE:
				{
					String polite = null;
					if (donation.getLink() == null)
					{
						if (donation.getAddress().getValidLinks().size() == 1)
						{
							final Person person = donation.getAddress().getValidLinks().iterator().next().getPerson();
							polite = person.getSex() == null ? "Fehler!" : PersonFormatter.getInstance()
									.replaceSalutationVariables(person, person.getSex().getForm(person.getForm()));
						}
						else
						{
							final AddressSalutation salutation = donation.getAddress().getSalutation();
							polite = salutation == null ? "" : salutation.getPolite();
							if (polite.isEmpty())
							{
								polite = "Sehr geehrte Damen und Herren";
							}
						}
					}
					else
					{
						if (donation.getLink().isDeleted() || donation.getLink().getPerson().isDeleted())
						{
							final AddressSalutation salutation = donation.getAddress().getSalutation();
							polite = salutation == null ? "" : salutation.getPolite();
							if (polite.isEmpty())
							{
								polite = "Sehr geehrte Damen und Herren";
							}
						}
						else
						{
							final Person person = donation.getLink().getPerson();
							polite = person.getSex() == null ? "Fehler!" : PersonFormatter.getInstance()
									.replaceSalutationVariables(person, person.getSex().getForm(person.getForm()));
						}
					}
					return polite;
				}
				case MAILING_ADDRESS:
				{
					if (donation.getLink() == null)
					{
						if (donation.getAddress().getValidLinks().size() == 1)
						{
							final LinkPersonAddress link = donation.getAddress().getValidLinks().iterator().next();
							return LinkPersonAddressFormatter.getInstance().getLabel(link);
						}
						else
						{
							return AddressFormatter.getInstance().formatAddressLabel(donation.getAddress());
						}
					}
					else
					{
						if (donation.getLink().isDeleted() || donation.getLink().getPerson().isDeleted())
						{
							return AddressFormatter.getInstance().formatAddressLabel(donation.getAddress());
						}
						else
						{
							final LinkPersonAddress link = donation.getLink();
							return LinkPersonAddressFormatter.getInstance().getLabel(link);
						}
					}
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public Class<?> getType()
		{
			if (this.equals(AMOUNT))
			{
				return Double.class;
			}
			if (this.equals(YEAR))
			{
				return Integer.class;
			}
			if (this.equals(DATE))
			{
				return Calendar.class;
			}
			if (this.equals(ID))
			{
				return Long.class;
			}
			return String.class;
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
