package ch.eugster.events.persistence.database;

import java.util.Map;

import javax.persistence.EntityManager;

import org.eclipse.persistence.sessions.Session;

public interface Connection
{
	EntityManager getEntityManager();
	
	Session getSession();
	
	Map<String, Object> getProperties();
	
	String getPersistenceUnitName();
	
	State getState();
	
	public enum State
	{
		SHUTDOWN, STARTING, RUNNING, ERROR;
	}
	
	public enum ConnectionPropertyType
	{
		NO_CHANGE, UPDATE_ONLY, DROP_AND_CREATE;
	}
	
}
