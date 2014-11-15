package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.util.List;
import java.util.Properties;

public interface DataMap
{
	Properties getProperties();
	
	String getProperty(String key);

	String getProperty(String key, String defaultValue);

	List<DataMap> getTableMaps(String key);
	
	void printHTML(Writer writer, String ref, String title);
}
