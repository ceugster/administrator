package ch.eugster.events.documents.maps;

import java.util.List;

public interface DataMap
{
	String getProperty(String key);

	String getProperty(String key, String defaultValue);

	List<DataMap> getTableMaps(String key);
}
