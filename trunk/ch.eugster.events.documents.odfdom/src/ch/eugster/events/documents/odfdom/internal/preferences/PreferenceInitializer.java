package ch.eugster.events.documents.odfdom.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.eugster.events.documents.odfdom.internal.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public static final String KEY_OFFICE_PACKAGE = "office.package";

	public static final String KEY_OFFICE_WRITER_PATH = "open.office.writer.path";

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
		store.setDefault(KEY_OFFICE_PACKAGE, "open.office");
		store.setDefault(KEY_OFFICE_WRITER_PATH, "");
	}
}
