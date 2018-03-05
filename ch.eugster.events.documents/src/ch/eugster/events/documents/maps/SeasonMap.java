package ch.eugster.events.documents.maps;

import java.util.Calendar;

import ch.eugster.events.persistence.model.Season;

public class SeasonMap extends AbstractDataMap<Season>
{
	protected SeasonMap() {
		super();
	}

	public SeasonMap(final Season season)
	{
		for (final Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(season));
		}
	}

	public enum Key implements DataMapKey
	{
		CODE, TITLE, START, END;

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
					return "Code der Saison";
				}
				case TITLE:
				{
					return "Titel der Saison";
				}
				case START:
				{
					return "Saisonbeginn";
				}
				case END:
				{
					return "Saisonende";
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
					return "season_code";
				}
				case TITLE:
				{
					return "season_title";
				}
				case START:
				{
					return "season_start";
				}
				case END:
				{
					return "season_end";
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
				case TITLE:
				{
					return "Titel";
				}
				case START:
				{
					return "Beginn";
				}
				case END:
				{
					return "Ende";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Season season)
		{
			switch (this)
			{
				case CODE:
				{
					return season.getCode();
				}
				case TITLE:
				{
					return season.getTitle();
				}
				case START:
				{
					final Calendar calendar = season.getStart();
					return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
				}
				case END:
				{
					final Calendar calendar = season.getEnd();
					return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
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
