package ch.eugster.events.course.reporting.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		prefs.put(PreferenceConstants.KEY_BOOKING_TEMPLATE, "");
		prefs.put(PreferenceConstants.KEY_INVITATION_TEMPLATE, "");
		prefs.put(PreferenceConstants.KEY_PARTICIPATION_TEMPLATE, "");
		prefs.put(PreferenceConstants.KEY_TEMPLATE_PATHS, "");
		prefs.put(PreferenceConstants.P_DEFAULT_EXPORT_FILE_DIRECTORY, System.getProperty("user.home"));
		prefs.putInt(PreferenceConstants.P_DEFAULT_FILE_FORMAT, Format.PDF.ordinal());
		prefs.putInt(PreferenceConstants.P_DESTINATION, Destination.PRINTER.ordinal());
		prefs.putBoolean(PreferenceConstants.P_USE_STANDARD_PRINTER, false);
	}

}
