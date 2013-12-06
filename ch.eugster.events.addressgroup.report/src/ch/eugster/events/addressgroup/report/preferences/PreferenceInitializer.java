package ch.eugster.events.addressgroup.report.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_DEFAULT_EXPORT_FILE_DIRECTORY, System.getProperty("user.home"));
		store.setDefault(PreferenceConstants.P_DEFAULT_FILE_FORMAT, Format.PDF.ordinal());
		store.setDefault(PreferenceConstants.P_DESTINATION, Destination.PRINTER.ordinal());
		store.setDefault(PreferenceConstants.P_PRINT_RECIPIENT_LIST_AUTOMATICALLY, true);
		store.setDefault(PreferenceConstants.P_USE_STANDARD_PRINTER, false);
	}
}
