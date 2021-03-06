package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Membership;

public class BookingTypeMap extends AbstractDataMap<BookingType>
{
	public BookingTypeMap(final BookingType bookingType)
	{
		for (final Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(bookingType));
		}
	}

	protected BookingTypeMap() {
		super();
	}

	public enum Key implements DataMapKey
	{
		CODE, NAME, ANNULATION_CHARGE, MAX_AGE, MEMBERSHIP, PRICE, PARTICIPANT_COUNT;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case CODE:
				{
					return "Code der Buchungsart";
				}
				case NAME:
				{
					return "Bezeichnung der Buchungsart";
				}
				case ANNULATION_CHARGE:
				{
					return "Annullationsgebühren";
				}
				case MAX_AGE:
				{
					return "Maximales Alter";
				}
				case MEMBERSHIP:
				{
					return "Mitgliedschaft";
				}
				case PRICE:
				{
					return "Preis";
				}
				case PARTICIPANT_COUNT:
				{
					return "Anzahl";
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
			return String.class;
		}

		@Override
		public String getKey()
		{
			switch (this)
			{
				case CODE:
				{
					return "booking_type_code";
				}
				case NAME:
				{
					return "booking_type_name";
				}
				case ANNULATION_CHARGE:
				{
					return "booking_type_annulation_charge";
				}
				case MAX_AGE:
				{
					return "booking_type_max_age";
				}
				case MEMBERSHIP:
				{
					return "booking_type_membership";
				}
				case PRICE:
				{
					return "booking_type_price";
				}
				case PARTICIPANT_COUNT:
				{
					return "count";
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
				case CODE:
				{
					return "Code";
				}
				case NAME:
				{
					return "Bezeichnung";
				}
				case ANNULATION_CHARGE:
				{
					return "Annullationsgebühren";
				}
				case MAX_AGE:
				{
					return "Alter";
				}
				case MEMBERSHIP:
				{
					return "Mitgliedschaft";
				}
				case PRICE:
				{
					return "Preis";
				}
				case PARTICIPANT_COUNT:
				{
					return "Anzahl";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final BookingType bookingType)
		{
			if (bookingType == null)
			{
				return "";
			}
			switch (this)
			{
				case CODE:
				{
					return bookingType.getCode() == null ? "" : bookingType.getCode();
				}
				case NAME:
				{
					return bookingType.getName() == null || bookingType.getName().isEmpty() ? "Betrag "
							+ Key.PRICE.getValue(bookingType) : bookingType.getName();
				}
				case ANNULATION_CHARGE:
				{
					return AbstractDataMap.getAmountFormatter().format(bookingType.getAnnulationCharges());
				}
				case MAX_AGE:
				{
					return AbstractDataMap.getIntegerFormatter().format(bookingType.getMaxAge());
				}
				case MEMBERSHIP:
				{
					final Membership membership = bookingType.getMembership();
					return membership == null ? "" : membership.getName();
				}
				case PRICE:
				{
					return AbstractDataMap.getAmountFormatter().format(bookingType.getPrice());
				}
				case PARTICIPANT_COUNT:
				{
					return AbstractDataMap.getIntegerFormatter().format(bookingType.getCourse().getParticipantsCount(bookingType));
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
