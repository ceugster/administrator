package ch.eugster.events.documents.maps;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import ch.eugster.events.persistence.model.Compensation;

public class CompensationMap extends AbstractDataMap<Compensation>
{
	protected CompensationMap() {
		super();
	}

	private static NumberFormat amountFormatter;

	public CompensationMap(final Compensation compensation)
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
			setProperty(key.getKey(), key.getValue(compensation));
		}
		this.setProperties(new CourseGuideMap(compensation.getCourseGuide(), false).getProperties());
	}

	public enum Key implements DataMapKey
	{
		AMOUNT, TYPE;

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case AMOUNT:
				{
					return "Betrag";
				}
				case TYPE:
				{
					return "Entschädigungsart";
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
				case AMOUNT:
				{
					return "compensation_amount";
				}
				case TYPE:
				{
					return "compensation_type";
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
				case AMOUNT:
				{
					return "Betrag";
				}
				case TYPE:
				{
					return "Art";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Compensation compensation)
		{
			switch (this)
			{
				case AMOUNT:
				{
					return amountFormatter.format(compensation.getAmount());
				}
				case TYPE:
				{
					return compensation.getCompensationType() == null ? "" : compensation.getCompensationType()
							.getName();
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
