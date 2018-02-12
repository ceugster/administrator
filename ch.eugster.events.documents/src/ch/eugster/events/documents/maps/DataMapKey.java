package ch.eugster.events.documents.maps;

public interface DataMapKey
{
	String getDescription();

	String getKey();
	
	Class<?> getType();

	String getName();
}
