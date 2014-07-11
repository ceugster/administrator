package ch.eugster.events.documents.maps;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractDataMap implements DataMap
{
	private Properties properties = new Properties();

	private Map<String, List<DataMap>> tableMaps = new HashMap<String, List<DataMap>>();

	public static DataMap getDataMap(Class<? extends AbstractDataMap> clazz)
	{
		DataMap map = null;
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
	
	protected void addTableMaps(final String key, final List<DataMap> dataMaps)
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
	public List<DataMap> getTableMaps(final String key)
	{
		List<DataMap> list = tableMaps.get(key);
		return list == null ? new ArrayList<DataMap>() : list;
	}

	public void setProperties(final Properties properties)
	{
		Set<String> keys = properties.stringPropertyNames();
		for (String key : keys)
		{
			this.properties.setProperty(key, properties.getProperty(key));
		}
	}

	protected void setProperty(final String key, final String value)
	{
		properties.setProperty(key, value);
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
}
