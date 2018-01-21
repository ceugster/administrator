package ch.eugster.events.donation.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.donation.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public static final String KEY_DONATION_TEMPLATE = "donation.confirmation.template";

	public static final String KEY_PRINT_OUT_KEYS = "print.out.file";

	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		prefs.put(KEY_DONATION_TEMPLATE, "");
	}

}
