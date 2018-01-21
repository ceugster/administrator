package ch.eugster.events.documents.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.documents.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.getDefault().getBundleContext().getBundle()
				.getSymbolicName());
		prefs.put(PreferenceConstants.KEY_PRINT_OUT_KEYS, "");
	}

}
