package ch.eugster.events.persistence.events;

import java.util.HashMap;
import java.util.Vector;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import ch.eugster.events.persistence.model.AbstractEntity;

public class EntityMediator
{
	private static HashMap<Class<? extends AbstractEntity>, Vector<EntityListener>> entityListeners = new HashMap<Class<? extends AbstractEntity>, Vector<EntityListener>>();

	private static EntityMediator instance;

	public static EntityMediator getInstance()
	{
		if (EntityMediator.instance == null)
			EntityMediator.instance = new EntityMediator();
		return EntityMediator.instance;
	}

	public EntityMediator()
	{
	}

	public static void addListener(Class<? extends AbstractEntity> clazz, EntityListener listener)
	{
		Vector<EntityListener> listeners = EntityMediator.entityListeners.get(clazz);
		if (listeners == null)
			listeners = new Vector<EntityListener>();

		listeners.add(listener);
		EntityMediator.entityListeners.put(clazz, listeners);
	}

	public static void removeListener(Class<? extends AbstractEntity> clazz, EntityListener listener)
	{
		Vector<EntityListener> listeners = EntityMediator.entityListeners.get(clazz);
		if (listeners != null)
			listeners.remove(listener);
		EntityMediator.entityListeners.put(clazz, listeners);
	}

	@PrePersist
	public void prePersist(AbstractEntity entity)
	{
		Vector<EntityListener> entityListeners = EntityMediator.entityListeners.get(entity.getClass());
		if (entityListeners != null && !entityListeners.isEmpty())
		{
			EntityListener[] listeners = entityListeners.toArray(new EntityListener[0]);
			for (EntityListener listener : listeners)
				listener.prePersist(entity);
		}
	}

	@PostPersist
	public void postPersist(AbstractEntity entity)
	{
		Vector<EntityListener> entityListeners = EntityMediator.entityListeners.get(entity.getClass());
		if (entityListeners != null && !entityListeners.isEmpty())
		{
			EntityListener[] listeners = entityListeners.toArray(new EntityListener[0]);
			for (EntityListener listener : listeners)
				listener.postPersist(entity);
		}
	}

	@PreUpdate
	public void preUpdate(AbstractEntity entity)
	{
		Vector<EntityListener> entityListeners = EntityMediator.entityListeners.get(entity.getClass());
		if (entityListeners != null && !entityListeners.isEmpty())
		{
			EntityListener[] listeners = entityListeners.toArray(new EntityListener[0]);
			for (EntityListener listener : listeners)
				if (entity.isDeleted())
					listener.preDelete(entity);
				else
					listener.preUpdate(entity);
		}
	}

	@PostUpdate
	public void postUpdate(AbstractEntity entity)
	{
		Vector<EntityListener> entityListeners = EntityMediator.entityListeners.get(entity.getClass());
		if (entityListeners != null && !entityListeners.isEmpty())
		{
			EntityListener[] listeners = entityListeners.toArray(new EntityListener[0]);
			for (EntityListener listener : listeners)
			{
				if (entity.isDeleted())
					listener.postDelete(entity);
				else
					listener.postUpdate(entity);
			}
		}
	}

	@PreRemove
	public void preRemove(AbstractEntity entity)
	{
		Vector<EntityListener> entityListeners = EntityMediator.entityListeners.get(entity.getClass());
		if (entityListeners != null && !entityListeners.isEmpty())
		{
			EntityListener[] listeners = entityListeners.toArray(new EntityListener[0]);
			for (EntityListener listener : listeners)
				listener.preRemove(entity);
		}
	}

	@PostRemove
	public void postRemove(AbstractEntity entity)
	{
		Vector<EntityListener> entityListeners = EntityMediator.entityListeners.get(entity.getClass());
		if (entityListeners != null && !entityListeners.isEmpty())
		{
			EntityListener[] listeners = entityListeners.toArray(new EntityListener[0]);
			for (EntityListener listener : listeners)
				listener.postRemove(entity);
		}
	}

	@PostLoad
	public void postLoad(AbstractEntity entity)
	{
		Vector<EntityListener> entityListeners = EntityMediator.entityListeners.get(entity.getClass());
		if (entityListeners != null && !entityListeners.isEmpty())
		{
			EntityListener[] listeners = entityListeners.toArray(new EntityListener[0]);
			for (EntityListener listener : listeners)
				listener.postLoad(entity);
		}
	}
}
