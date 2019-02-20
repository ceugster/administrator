package ch.eugster.events.persistence.service;

import javax.persistence.EntityManager;

import org.eclipse.persistence.sessions.Session;
import org.osgi.service.component.ComponentContext;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.IEntity;
import ch.eugster.events.persistence.queries.AbstractEntityQuery;

public interface ConnectionService
{
	EntityManager getEntityManager();

	Session getSession();

	void connect();

	ComponentContext getContext();

	AbstractEntityQuery<? extends AbstractEntity> getQuery(Class<? extends AbstractEntity> clazz);

	AbstractEntity find(Class<AbstractEntity> clazz, Long id);

	AbstractEntity refresh(IEntity entity);

	void log(int level, String message);
}
