package ch.eugster.events.documents.maps;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ch.eugster.events.persistence.model.AbstractEntity;

public abstract class AbstractDataMap<T extends AbstractEntity> implements DataMap<T>
{
	private Properties properties = new Properties();

	private Map<String, List<DataMap<?>>> tableMaps = new HashMap<String, List<DataMap<?>>>();

	public static DataMap<?> getDataMap(Class<? extends AbstractDataMap<?>> clazz)
	{
		DataMap<?> map = null;
		try 
		{
			map = clazz.newInstance();
		} 
		catch (InstantiationException e) 
		{
		} 
		catch (IllegalAccessException e) 
		{
		}
		return map;
	}
	
	protected AbstractDataMap()
	{
		
	}
	
	protected void addTableMaps(final String key, final List<DataMap<?>> dataMaps)
	{
		this.tableMaps.put(key, dataMaps);
	}

	public Properties getProperties()
	{
		return this.properties;
	}

	@Override
	public String getProperty(final String key)
	{
		return properties.getProperty(key);
	}

	@Override
	public String getProperty(final String key, final String defaultValue)
	{
		String value = getProperty(key);
		return value == null ? defaultValue : value;
	}

	@Override
	public List<DataMap<?>> getTableMaps(final String key)
	{
		List<DataMap<?>> list = tableMaps.get(key);
		return list == null ? new ArrayList<DataMap<?>>() : list;
	}

	public void setProperties(final Properties properties)
	{
		Set<String> keys = properties.stringPropertyNames();
		for (String key : keys)
		{
			this.properties.setProperty(key, properties.getProperty(key));
		}
	}

	public void setProperty(final String key, final String value)
	{
		properties.setProperty(key, value);
	}

	@Override
	public int compareTo(DataMap<T> other) 
	{
		return 0;
	}

	protected abstract DataMapKey[] getKeys();

	public void printHTML(Writer writer, String key, String value)
	{
		printHeader(writer, 1, key, value);
		printKeys(writer);
		printReferences(writer);
		printTables(writer);
		endTable(writer);

	}
	
	protected void printKeys(Writer writer)
	{
		this.printHeader(writer, 2, "Schlüsselwörter");
		startTable(writer, 0);
		startTableRow(writer);
		printHeaderCell(writer, "Bezeichnung");
		printHeaderCell(writer, "Bedeutung");
		endTableRow(writer);
		for (DataMapKey key : getKeys())
		{
			startTableRow(writer);
			printCell(writer, key.getKey());
			printCell(writer, key.getDescription());
			endTableRow(writer);
		}
		endTable(writer);
	}

	protected void printReferences(Writer writer)
	{
		
	}

	protected void printTables(Writer writer)
	{
		
	}

	protected void startTableRow(Writer writer)
	{
		try
		{
			writer.write("\t\t\t<tr>\n");
		}
		catch (IOException e)
		{

		}
	}

	protected void endTableRow(Writer writer)
	{
		try
		{
			writer.write("\t\t\t</tr>\n");
		}
		catch (IOException e)
		{

		}
	}

	protected void startTable(Writer writer, int border)
	{
		try
		{
			writer.write("\t\t\t<table border\"" + border + "\">\n");
		}
		catch (IOException e)
		{

		}
	}

	protected void endTable(Writer writer)
	{
		try
		{
			writer.write("\t\t</table>\n");
		}
		catch (IOException e)
		{

		}
	}

	protected void printHeaderCell(Writer writer, String value)
	{
		try
		{
			writer.write("\t\t\t\t<th align=\"left\">\n");
			writer.write("\t\t\t\t\t" + value + "\n");
			writer.write("\t\t\t\t</th>\n");
		}
		catch (IOException e)
		{

		}
	}

	protected void printCell(Writer writer, String value)
	{
		printCell(writer, null, value);
	}

	protected void printCell(Writer writer, String ref, String value)
	{
		try
		{
			writer.write("\t\t\t\t<td>\n");
			writer.write("\t\t\t\t\t" + (ref == null ? "" : "<a href=\"" + ref + "\">") + value
					+ (ref == null ? "" : "</a>") + "\n");
			writer.write("\t\t\t\t</td>\n");
		}
		catch (IOException e)
		{

		}
	}

	protected void printHeader(Writer writer, int level, String title)
	{
		printHeader(writer, level, null, title);
	}

	protected void printHeader(Writer writer, int level, String ref, String title)
	{
		try
		{
			writer.write("\t\t<h" + level + ">" + (ref == null ? "" : "<a name=\"" + ref + "\">") + title
					+ (ref == null ? "" : "</a>") + "</h" + level + ">\n");
		}
		catch (IOException e)
		{

		}
	}

	public enum WeekdayType
	{
		NONE, SHORT, LONG;
	}
	
	public enum Weekday
	{
		SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
		
		public static Weekday getWeekday(int dayOfWeek)
		{
			return Weekday.values()[dayOfWeek];
		}
		
		public String getWeekday(WeekdayType weekdayType)
		{
			if (weekdayType == null) return "";
			switch(weekdayType)
			{
			case NONE:
			{
				return "";
			}
			case SHORT:
			{
				return this.getCode();
			}
			case LONG:
			{
				return this.getName();
			}
			default:
			{
				return "";
			}
			}
		}
		
		public String getCode()
		{
			switch (this)
			{
			case MONDAY:
			{
				return "Mo";
			}
			case TUESDAY:
			{
				return "Di";
			}
			case WEDNESDAY:
			{
				return "Mi";
			}
			case THURSDAY:
			{
				return "Do";
			}
			case FRIDAY:
			{
				return "Fr";
			}
			case SATURDAY:
			{
				return "Sa";
			}
			case SUNDAY:
			{
				return "So";
			}
			default:
			{
				return "";
			}
			}
		}

		public String getName()
		{
			switch (this)
			{
			case MONDAY:
			{
				return "Montag";
			}
			case TUESDAY:
			{
				return "Dienstag";
			}
			case WEDNESDAY:
			{
				return "Mittwoch";
			}
			case THURSDAY:
			{
				return "Donnerstag";
			}
			case FRIDAY:
			{
				return "Freitag";
			}
			case SATURDAY:
			{
				return "Samstag";
			}
			case SUNDAY:
			{
				return "Sonntag";
			}
			default:
			{
				return "";
			}
			}
		}
	}
	
	public enum MonthType
	{
		NONE, SHORT, LONG;
	}
	
	public enum Month
	{
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
		
		public static Month getMonth(int monthNumber)
		{
			return Month.values()[monthNumber];
		}
		
		public String getMonth(MonthType monthType)
		{
			if (monthType == null) return "";
			switch(monthType)
			{
			case NONE:
			{
				return "";
			}
			case SHORT:
			{
				return this.getCode();
			}
			case LONG:
			{
				return this.getName();
			}
			default:
			{
				return "";
			}
			}
		}
		
		public String getCode()
		{
			switch (this)
			{
			case JANUARY:
			{
				return "Jan";
			}
			case FEBRUARY:
			{
				return "Feb";
			}
			case MARCH:
			{
				return "Mär";
			}
			case APRIL:
			{
				return "Apr";
			}
			case MAY:
			{
				return "Mai";
			}
			case JUNE:
			{
				return "Jun";
			}
			case JULY:
			{
				return "Jul";
			}
			case AUGUST:
			{
				return "Aug";
			}
			case SEPTEMBER:
			{
				return "Sep";
			}
			case OCTOBER:
			{
				return "Okt";
			}
			case NOVEMBER:
			{
				return "Nov";
			}
			case DECEMBER:
			{
				return "Dez";
			}
			default:
			{
				return "";
			}
			}
		}

		public String getName()
		{
			switch (this)
			{
			case JANUARY:
			{
				return "Januar";
			}
			case FEBRUARY:
			{
				return "Februar";
			}
			case MARCH:
			{
				return "März";
			}
			case APRIL:
			{
				return "April";
			}
			case MAY:
			{
				return "Mai";
			}
			case JUNE:
			{
				return "Juni";
			}
			case JULY:
			{
				return "Juli";
			}
			case AUGUST:
			{
				return "August";
			}
			case SEPTEMBER:
			{
				return "September";
			}
			case OCTOBER:
			{
				return "Oktober";
			}
			case NOVEMBER:
			{
				return "November";
			}
			case DECEMBER:
			{
				return "Dezember";
			}
			default:
			{
				return "";
			}
			}
		}
	}
}
