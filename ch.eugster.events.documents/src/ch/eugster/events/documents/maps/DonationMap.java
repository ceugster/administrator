package ch.eugster.events.documents.maps;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Locale;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class DonationMap extends AbstractDataMap
{
	private static final DateFormat dateFormatter = SimpleDateFormat.getDateInstance();

	private static NumberFormat numberFormatter;

	private static NumberFormat integerFormatter = DecimalFormat.getIntegerInstance();

	public DonationMap(final Donation donation)
	{
		if (numberFormatter == null)
		{
			Currency currency = Currency.getInstance(Locale.getDefault());
			numberFormatter = DecimalFormat.getNumberInstance();
			numberFormatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
			numberFormatter.setMinimumFractionDigits(currency.getDefaultFractionDigits());
		}
		for (Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(donation));
		}
	}

	public enum Key implements DataMapKey
	{
		ID, DATE, AMOUNT, PURPOSE_CODE, PURPOSE_NAME, PURPOSE_DESCRIPTION, YEAR, ANOTHER_LINE, SALUTATION, POLITE, MAILING_ADDRESS;;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case ID:
				{
					return "Identifikationsnummer der Spende";
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
					return "donation_id";
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
					return donation.getId().toString();
				}
				case DATE:
				{
					return dateFormatter.format(donation.getDonationDate().getTime());
				}
				case AMOUNT:
				{
					return numberFormatter.format(donation.getAmount());
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
					return integerFormatter.format(donation.getYear());
				}
				case ANOTHER_LINE:
				{
					String anotherLine = "";
					if (donation.getLink() == null)
					{
						anotherLine = donation.getAddress().getAnotherLine();
					}
					else
					{
						anotherLine = donation.getLink().getPerson().getAnotherLine();
					}
					return anotherLine;
				}
				case SALUTATION:
				{
					if (donation.getLink() == null)
					{
						return donation.getAddress().getSalutation() == null ? "" : donation.getAddress().getSalutation()
								.getSalutation();
					}
					else
					{
						return donation.getLink().getPerson().getSex().getSalutation();
					}
				}
				case POLITE:
				{
					String polite = null;
					if (donation.getLink() == null)
					{
						AddressSalutation salutation = donation.getAddress().getSalutation();
						polite = salutation == null ? "" : salutation.getPolite();
						if (polite.isEmpty())
						{
							polite = "Sehr geehrte Damen und Herren";
						}
					}
					else
					{
						Person person = donation.getLink().getPerson();
						polite = PersonFormatter.getInstance().replaceSalutationVariables(person,
								person.getSex().getForm(person.getForm()));
					}
					return polite;
				}
				case MAILING_ADDRESS:
				{
					if (donation.getLink() == null)
					{
						return AddressFormatter.getInstance().formatAddressLabel(donation.getAddress());
					}
					else
					{
						LinkPersonAddress link = donation.getLink();
						return LinkPersonAddressFormatter.getInstance().getLabel(link);
					}
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}
}
