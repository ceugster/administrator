package ch.eugster.events.documents.maps;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ch.eugster.events.persistence.model.AbstractEntity;

public abstract class AbstractDataMap<T extends AbstractEntity> implements DataMap<T>
{
	private static DateFormat dateTimeFormatter;

	private static DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

	private static DateFormat timeFormatter = new SimpleDateFormat("HH:mm");

	private static NumberFormat integerFormatter;

	private static NumberFormat amountFormatter;
	
	public static NumberFormat getAmountFormatter()
	{
		if (AbstractDataMap.amountFormatter == null)
		{
			AbstractDataMap.amountFormatter = NumberFormat.getNumberInstance();
			AbstractDataMap.amountFormatter.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
			AbstractDataMap.amountFormatter.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
			AbstractDataMap.amountFormatter.setGroupingUsed(false);
		}
		return AbstractDataMap.amountFormatter;
	};
	
	public static DateFormat getDateTimeFormatter()
	{
		if (AbstractDataMap.dateTimeFormatter == null)
		{
			AbstractDataMap.dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		}
		return AbstractDataMap.dateTimeFormatter;
	}
		
	public static DateFormat getDateFormatter()
	{
		if (AbstractDataMap.dateFormatter == null)
		{
			AbstractDataMap.dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
		}
		return AbstractDataMap.dateFormatter;
	}
		
	public static DateFormat getTimeFormatter()
	{
		if (AbstractDataMap.timeFormatter == null)
		{
			AbstractDataMap.timeFormatter = new SimpleDateFormat("HH:mm");
		}
		return AbstractDataMap.timeFormatter;
	}
		
	public static NumberFormat getIntegerFormatter()
	{
		if (AbstractDataMap.integerFormatter == null)
		{
			AbstractDataMap.integerFormatter = NumberFormat.getIntegerInstance();
		}
		return AbstractDataMap.integerFormatter;
	}

	private final Properties properties = new Properties();

	private final Map<String, List<DataMap<?>>> tableMaps = new HashMap<String, List<DataMap<?>>>();

	public static DataMap<?> getDataMap(final Class<? extends AbstractDataMap<?>> clazz)
	{
		DataMap<?> map = null;
		try 
		{
			map = clazz.newInstance();
		} 
		catch (final InstantiationException e) 
		{
		} 
		catch (final IllegalAccessException e) 
		{
		}
		return map;
	}
	
	protected AbstractDataMap()
	{
		
	}
	
	public ch.eugster.events.documents.maps.DataMapKey[] getTableKeys()
	{
		return new ch.eugster.events.documents.maps.DataMapKey[0];
	}
	
	protected void addTableMaps(final String key, final List<DataMap<?>> dataMaps)
	{
		this.tableMaps.put(key, dataMaps);
	}

	@Override
	public Properties getProperties()
	{
		return this.properties;
	}

	@Override
	public String getProperty(final String key)
	{
		return this.properties.getProperty(key);
	}

	@Override
	public String getProperty(final String key, final String defaultValue)
	{
		final String value = this.getProperty(key);
		return value == null ? defaultValue : value;
	}

	@Override
	public List<DataMap<?>> getTableMaps(final String key)
	{
		final List<DataMap<?>> list = this.tableMaps.get(key);
		return list == null ? new ArrayList<DataMap<?>>() : list;
	}

	public void setProperties(final Properties properties)
	{
		final Set<String> keys = properties.stringPropertyNames();
		for (final String key : keys)
		{
			this.properties.setProperty(key, properties.getProperty(key));
		}
	}

	@Override
	public void setProperty(final String key, final String value)
	{
		this.properties.setProperty(key, value);
	}

	@Override
	public int compareTo(final DataMap<T> other) 
	{
		return 0;
	}

	protected abstract DataMapKey[] getKeys();

	@Override
	public void printHTML(final Writer writer, final String key, final String value)
	{
		this.printHeader(writer, 1, key, value);
		this.printKeys(writer);
		this.printReferences(writer);
		this.printTables(writer);
		this.endTable(writer);

	}
	
	protected void printKeys(final Writer writer)
	{
		this.printHeader(writer, 2, "Schlüsselwörter");
		this.startTable(writer, 0);
		this.startTableRow(writer);
		this.printHeaderCell(writer, "Bezeichnung");
		this.printHeaderCell(writer, "Bedeutung");
		this.endTableRow(writer);
		for (final DataMapKey key : this.getKeys())
		{
			this.startTableRow(writer);
			this.printCell(writer, key.getKey());
			this.printCell(writer, key.getDescription());
			this.endTableRow(writer);
		}
		this.endTable(writer);
	}

	protected void printReferences(final Writer writer)
	{
		
	}

	protected void printTables(final Writer writer)
	{
		
	}

	protected void startTableRow(final Writer writer)
	{
		try
		{
			writer.write("\t\t\t<tr>\n");
		}
		catch (final IOException e)
		{

		}
	}

	protected void endTableRow(final Writer writer)
	{
		try
		{
			writer.write("\t\t\t</tr>\n");
		}
		catch (final IOException e)
		{

		}
	}

	protected void startTable(final Writer writer, final int border)
	{
		try
		{
			writer.write("\t\t\t<table border\"" + border + "\">\n");
		}
		catch (final IOException e)
		{

		}
	}

	protected void endTable(final Writer writer)
	{
		try
		{
			writer.write("\t\t</table>\n");
		}
		catch (final IOException e)
		{

		}
	}

	protected void printHeaderCell(final Writer writer, final String value)
	{
		try
		{
			writer.write("\t\t\t\t<th align=\"left\">\n");
			writer.write("\t\t\t\t\t" + value + "\n");
			writer.write("\t\t\t\t</th>\n");
		}
		catch (final IOException e)
		{

		}
	}

	protected void printCell(final Writer writer, final String value)
	{
		this.printCell(writer, null, value);
	}

	protected void printCell(final Writer writer, final String ref, final String value)
	{
		try
		{
			writer.write("\t\t\t\t<td>\n");
			writer.write("\t\t\t\t\t" + (ref == null ? "" : "<a href=\"" + ref + "\">") + value
					+ (ref == null ? "" : "</a>") + "\n");
			writer.write("\t\t\t\t</td>\n");
		}
		catch (final IOException e)
		{

		}
	}

	protected void printHeader(final Writer writer, final int level, final String title)
	{
		this.printHeader(writer, level, null, title);
	}

	protected void printHeader(final Writer writer, final int level, final String ref, final String title)
	{
		try
		{
			writer.write("\t\t<h" + level + ">" + (ref == null ? "" : "<a name=\"" + ref + "\">") + title
					+ (ref == null ? "" : "</a>") + "</h" + level + ">\n");
		}
		catch (final IOException e)
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
		
		public static Weekday getWeekday(final int dayOfWeek)
		{
			return Weekday.values()[dayOfWeek];
		}
		
		public String getWeekday(final WeekdayType weekdayType)
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
		
		public static Month getMonth(final int monthNumber)
		{
			return Month.values()[monthNumber];
		}
		
		public String getMonth(final MonthType monthType)
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
