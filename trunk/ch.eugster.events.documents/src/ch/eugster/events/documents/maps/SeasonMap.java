package ch.eugster.events.documents.maps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ch.eugster.events.persistence.model.Season;

public class SeasonMap extends AbstractDataMap
{
	private static DateFormat dateFormatter = SimpleDateFormat.getDateInstance();

	public SeasonMap(final Season season)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(season));
		}
	}

	public enum Key implements DataMapKey
	{
		CODE, TITLE, START, END;

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
					Calendar calendar = season.getStart();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case END:
				{
					Calendar calendar = season.getEnd();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}
}
