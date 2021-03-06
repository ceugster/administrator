package ch.eugster.events.documents.maps;

import java.io.Writer;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Guide;

public class GuideMap extends AbstractDataMap<Guide>
{
	protected GuideMap() {
		super();
	}

	public GuideMap(final Guide guide)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(guide));
		}
		setProperties(new LinkMap(guide.getLink()).getProperties());
	}

	protected void printReferences(Writer writer)
	{
		printHeader(writer, 2, "Referenzen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, "#link", "Link Person/Adresse");
		endTableRow(writer);
		endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		DESCRIPTION, PHONE;

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
				case DESCRIPTION:
				{
					return "Beschreibung";
				}
				case PHONE:
				{
					return "Telefon";
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
				case DESCRIPTION:
				{
					return "guide_description";
				}
				case PHONE:
				{
					return "guide_phone";
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
				case DESCRIPTION:
				{
					return "Beschreibung";
				}
				case PHONE:
				{
					return "Telefon";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Guide guide)
		{
			switch (this)
			{
				case DESCRIPTION:
				{
					return guide.getDescription();
				}
				case PHONE:
				{
					return PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(
							guide.getLink().getPerson().getCountry(), guide.getPhone());
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
