package ch.eugster.events.persistence.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.service.prefs.Preferences;

import ch.eugster.events.persistence.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	public static final String KEY_STARTUP = "startup.code";

	public static final String KEY_DERBY_SYSTEM_HOME = "derby.system.home";

	public static final String KEY_USER_HOME = "user.home";

	public static String getServerQualifier()
	{
		return Activator.PLUGIN_ID + "/" + Activator.PERSISTENCE_UNIT_NAME;
	}

	public static Preferences getServerNode()
	{
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PERSISTENCE_UNIT_NAME);
	}

	@Override
	public void initializeDefaultPreferences()
	{
		Preferences prefs = PreferenceInitializer.getServerNode();
		prefs.putInt(PreferenceInitializer.KEY_STARTUP, 0);
		prefs.put(PersistenceUnitProperties.JDBC_DRIVER, org.postgresql.Driver.class.getName());
		prefs.put(PersistenceUnitProperties.JDBC_URL, "jdbc:postgresql:events");
		prefs.put(PersistenceUnitProperties.JDBC_USER, "events");
		prefs.put(PersistenceUnitProperties.JDBC_PASSWORD, "events");
	}

}
