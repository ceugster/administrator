package ch.eugster.events.persistence.events;

import ch.eugster.events.persistence.model.AbstractEntity;

public class EntityAdapter implements EntityListener
{

	@Override
	public void postDelete(AbstractEntity entity)
	{
	}

	@Override
	public void postLoad(AbstractEntity entity)
	{
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
	}

	@Override
	public void postRemove(AbstractEntity entity)
	{
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
	}

	@Override
	public void preDelete(AbstractEntity entity)
	{
	}

	@Override
	public void prePersist(AbstractEntity entity)
	{
	}

	@Override
	public void preRemove(AbstractEntity entity)
	{
	}

	@Override
	public void preUpdate(AbstractEntity entity)
	{
	}
}
