package ch.eugster.events.visits.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.visits.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public static final String KEY_DEFAULT_ADDRESS_TYPE = "default.address.type";

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
	}

}
