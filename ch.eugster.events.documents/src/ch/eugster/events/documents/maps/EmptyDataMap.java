package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.AbstractEntity;


public final class EmptyDataMap extends AbstractDataMap<AbstractEntity>
{

	@Override
	protected DataMapKey[] getKeys() 
	{
		return new DataMapKey[0];
	}

}
