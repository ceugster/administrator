package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Guide;

public class GuideMap extends AbstractDataMap
{
	public GuideMap(final Guide guide)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(guide));
		}
		setProperties(new LinkMap(guide.getLink()).getProperties());
	}

	public enum Key implements DataMapKey
	{
		DESCRIPTION, PHONE;

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
}
