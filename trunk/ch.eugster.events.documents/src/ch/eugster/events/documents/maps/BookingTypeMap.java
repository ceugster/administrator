package ch.eugster.events.documents.maps;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Membership;

public class BookingTypeMap extends AbstractDataMap
{
	private static NumberFormat amountFormatter;

	private static NumberFormat integerFormatter = DecimalFormat.getIntegerInstance();

	public BookingTypeMap(final BookingType bookingType)
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
			setProperty(key.getKey(), key.getValue(bookingType));
		}
		// setProperties(new
		// CourseMap(bookingType.getCourse()).getProperties());
	}

	protected BookingTypeMap() {
		super();
	}

	public enum Key implements DataMapKey
	{
		CODE, NAME, ANNULATION_CHARGE, MAX_AGE, MEMBERSHIP, PRICE;

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
					return "Annulationsgebühren";
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
					return "Annulationsgebühren";
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
					return amountFormatter.format(bookingType.getAnnulationCharges());
				}
				case MAX_AGE:
				{
					return integerFormatter.format(bookingType.getMaxAge());
				}
				case MEMBERSHIP:
				{
					Membership membership = bookingType.getMembership();
					return membership == null ? "" : membership.getName();
				}
				case PRICE:
				{
					return amountFormatter.format(bookingType.getPrice());
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
