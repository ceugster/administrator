package ch.eugster.events.charity.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.charity.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer 
{
	public static final String KEY_READER_ADDRESS = "reader.address";

	public static final String KEY_REACHABLE_TIMEOUT = "reachable.timeout";
	
	public static final String KEY_TIME_BETWEEN_READS = "time.between.reads";

	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		prefs.put(KEY_READER_ADDRESS, "speedwayr-10-a5-e4");
		prefs.put(KEY_REACHABLE_TIMEOUT, "10000");
		prefs.put(KEY_TIME_BETWEEN_READS, "180000");
	}

}
