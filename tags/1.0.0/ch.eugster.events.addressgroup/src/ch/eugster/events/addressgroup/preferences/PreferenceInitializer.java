package ch.eugster.events.addressgroup.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.addressgroup.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public static final String KEY_FORM_LETTER_FOLDER = "form.letter.folder";

	public static final String KEY_PRINT_OUT_KEYS = "print.out.file";

	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		prefs.put(KEY_FORM_LETTER_FOLDER, "");
	}

}
