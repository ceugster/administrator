package ch.eugster.events.documents.poi.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.eugster.events.documents.poi.internal.Activator;

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
		store.setDefault(PreferenceConstants.KEY_SPREADSHEET_PATH, "");
		store.setDefault(PreferenceConstants.KEY_OFFICE_PACKAGE, "microsoft.office");
		store.setDefault(PreferenceConstants.KEY_OFFICE_WRITER_PATH, "");
	}

}
