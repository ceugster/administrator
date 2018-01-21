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

	List<DataMap<?>> getTableMaps(String key);
	
	void printHTML(Writer writer, String ref, String title);
}
