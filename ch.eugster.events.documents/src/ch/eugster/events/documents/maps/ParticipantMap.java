package ch.eugster.events.documents.maps;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;

public class ParticipantMap extends AbstractDataMap
{
	private static NumberFormat numberFormatter;

	public ParticipantMap(final Participant participant)
	{
		if (numberFormatter == null)
		{
			Currency currency = Currency.getInstance(Locale.getDefault());
			numberFormatter = DecimalFormat.getNumberInstance();
			numberFormatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
			numberFormatter.setMinimumFractionDigits(currency.getDefaultFractionDigits());
		}
		this.setProperties(new LinkMap(participant.getLink()).getProperties());
		this.setProperties(new BookingTypeMap(participant.getBookingType()).getProperties());
	}

	public enum Key implements DataMapKey
	{
		ID, BOOKING_TYPE_CODE, BOOKING_TYPE_NAME, PRICE, ANOTHER_LINE, SALUTATION, POLITE, MAILING_ADDRESS;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case ID:
				{
					return "Id";
				}
				case BOOKING_TYPE_CODE:
				{
					return "Code Buchungsart";
				}
				case BOOKING_TYPE_NAME:
				{
					return "Bezeichnung Buchungsart";
				}
				case PRICE:
				{
					return "Betrag";
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
					return "participant_id";
				}
				case BOOKING_TYPE_CODE:
				{
					return "participant_booking_type_code";
				}
				case BOOKING_TYPE_NAME:
				{
					return "participant_booking_type_name";
				}
				case PRICE:
				{
					return "participant_price";
				}
				case ANOTHER_LINE:
				{
					return "participant_another_line";
				}
				case SALUTATION:
				{
					return "participant_salutation";
				}
				case POLITE:
				{
					return "participant_polite";
				}
				case MAILING_ADDRESS:
				{
					return "participant_mailing_address";
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
				case BOOKING_TYPE_CODE:
				{
					return "Code";
				}
				case BOOKING_TYPE_NAME:
				{
					return "Bezeichnung";
				}
				case PRICE:
				{
					return "Betrag";
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

		public String getValue(final Participant participant)
		{
			switch (this)
			{
				case ID:
				{
					return participant.getLink().getPerson().getId().toString();
				}
				case BOOKING_TYPE_CODE:
				{
					return participant.getBookingType().getCode();
				}
				case BOOKING_TYPE_NAME:
				{
					return participant.getBookingType().getName();
				}
				case PRICE:
				{
					return numberFormatter.format(participant.getBookingType().getPrice());
				}
				case ANOTHER_LINE:
				{
					return participant.getLink().getPerson().getAnotherLine();
				}
				case SALUTATION:
				{
					return participant.getLink().getPerson().getSex().getSalutation();
				}
				case POLITE:
				{
					Person person = participant.getLink().getPerson();
					return PersonFormatter.getInstance().replaceSalutationVariables(person,
							person.getSex().getForm(person.getForm()));
				}
				case MAILING_ADDRESS:
				{
					LinkPersonAddress link = participant.getLink();
					return LinkPersonAddressFormatter.getInstance().getLabel(link);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

}
