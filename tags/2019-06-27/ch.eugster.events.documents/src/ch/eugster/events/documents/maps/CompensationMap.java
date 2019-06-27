package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.Compensation;

public class CompensationMap extends AbstractDataMap<Compensation>
{
	protected CompensationMap() {
		super();
	}

	public CompensationMap(final Compensation compensation)
	{
		for (final Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(compensation));
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
					return AbstractDataMap.getAmountFormatter().format(compensation.getAmount());
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
