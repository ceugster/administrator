package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.util.List;
import java.util.Properties;

import ch.eugster.events.persistence.model.AbstractEntity;

public interface DataMap<T extends AbstractEntity> extends Comparable<DataMap<T>>
{
	Properties getProperties();
	
	String getProperty(String key);

	String getProperty(String key, String defaultValue);

	void setProperty(String key, String value);
	
	void setProperties(Properties properties);
	
	List<DataMap<?>> getTableMaps(String key);
	
	ch.eugster.events.documents.maps.DataMapKey[] getTableKeys();
	
	void printHTML(Writer writer, String ref, String title);
}
