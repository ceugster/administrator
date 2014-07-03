package ch.eugster.events.documents.maps;


public final class EmptyDataMap extends AbstractDataMap
{

	@Override
	protected DataMapKey[] getKeys() 
	{
		return new DataMapKey[0];
	}

}
