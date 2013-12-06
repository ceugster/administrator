package ch.eugster.events.documents.maps;

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

	protected void setProperties(final Properties properties)
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

}
