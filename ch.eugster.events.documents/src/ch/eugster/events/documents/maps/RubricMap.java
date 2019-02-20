package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.Rubric;

public class RubricMap extends AbstractDataMap<Rubric>
{
	protected RubricMap() {
		super();
	}

	public RubricMap(final Rubric rubric)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(rubric));
		}
	}

	public enum Key implements DataMapKey
	{
		CODE, NAME, DESCRIPTION;

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
				case CODE:
				{
					return "Code der Rubrik";
				}
				case NAME:
				{
					return "Bezeichnung der Rubrik";
				}
				case DESCRIPTION:
				{
					return "Beschreibung der Rubrik";
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
					return "rubric_code";
				}
				case NAME:
				{
					return "rubric_name";
				}
				case DESCRIPTION:
				{
					return "rubric_description";
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
				case DESCRIPTION:
				{
					return "Beschreibung";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Rubric rubric)
		{
			switch (this)
			{
				case CODE:
				{
					return rubric.getCode();
				}
				case NAME:
				{
					return rubric.getName();
				}
				case DESCRIPTION:
				{
					return rubric.getDescription();
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
