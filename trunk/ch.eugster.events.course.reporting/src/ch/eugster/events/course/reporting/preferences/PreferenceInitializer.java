package ch.eugster.events.course.reporting.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.course.reporting.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public static final String KEY_BOOKING_TEMPLATE = "booking.confirmation.template";

	public static final String KEY_INVITATION_TEMPLATE = "invitation.template";

	public static final String KEY_PARTICIPATION_TEMPLATE = "participation.confirmation.template";

	public static final String KEY_TEMPLATE_PATHS = "template.paths";

	public static final String KEY_PRINT_OUT_KEYS = "print.out.file";

	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		prefs.put(KEY_BOOKING_TEMPLATE, "");
		prefs.put(KEY_INVITATION_TEMPLATE, "");
		prefs.put(KEY_PARTICIPATION_TEMPLATE, "");
		prefs.put(KEY_TEMPLATE_PATHS, "");
	}

}