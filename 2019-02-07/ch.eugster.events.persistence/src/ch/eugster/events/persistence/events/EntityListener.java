package ch.eugster.events.persistence.events;

import ch.eugster.events.persistence.model.AbstractEntity;

public interface EntityListener
{
	void prePersist(AbstractEntity entity);

	void postPersist(AbstractEntity entity);

	void preUpdate(AbstractEntity entity);

	void postUpdate(AbstractEntity entity);

	void preDelete(AbstractEntity entity);

	void postDelete(AbstractEntity entity);

	void preRemove(AbstractEntity entity);

	void postRemove(AbstractEntity entity);

	void postLoad(AbstractEntity entity);
}
