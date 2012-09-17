package ch.eugster.events.person.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.EditorSelector;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public static final String KEY_ID_FORMAT = "person.id.format";

	public static final String KEY_PERSON_LABEL_FORMAT = "person.label.format";

	public static final String KEY_ADDRESS_LABEL_FORMAT = "address.label.format";

	public static final String KEY_DOMAIN_MANDATORY = "person.domain.mandatory";

	public static final String KEY_USE_DOMAIN = "person.use.domain";

	public static final String KEY_TITLE_FEMALE = "person.title.female";

	public static final String KEY_TITLE_MALE = "person.title.male";

	public static final String KEY_MAX_RECORDS = "person.max.records";

	public static final String KEY_EDITOR_SECTION_BEHAVIOUR = "editor.section.behaviour";

	public static final String KEY_EDITOR_SELECTOR = "editor.selector";

	public static final String EDITOR_SECTION_BEHAVIOUR_EDITOR = "0";

	public static final String EDITOR_SECTION_BEHAVIOUR_OBJECT = "1";

	public static final String KEY_PRINT_OUT_KEYS = "print.out.file";

	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		prefs.put(KEY_ID_FORMAT, PersonSettings.getInstance().getIdFormat());
		String label = PersonSettings.getInstance().getPersonLabelFormat();
		label = label.isEmpty() ? PersonFormatter.getInstance().createVisiblePersonLabel() : PersonFormatter
				.getInstance().convertPersonLabelToVisible(label);
		prefs.put(KEY_PERSON_LABEL_FORMAT, label);
		label = PersonSettings.getInstance().getAddressLabelFormat();
		label = label.isEmpty() ? AddressFormatter.getInstance().createVisibleAddressLabel() : AddressFormatter
				.getInstance().convertAddressLabelToVisible(label);
		prefs.put(KEY_ADDRESS_LABEL_FORMAT, label);
		prefs.put(KEY_USE_DOMAIN, Boolean.toString(PersonSettings.getInstance().getPersonHasDomain()));
		prefs.put(KEY_DOMAIN_MANDATORY, Boolean.toString(PersonSettings.getInstance().isPersonDomainMandatory()));
		prefs.putInt(KEY_MAX_RECORDS, 100);
		prefs.put(KEY_EDITOR_SECTION_BEHAVIOUR, EDITOR_SECTION_BEHAVIOUR_EDITOR);
		prefs.put(KEY_EDITOR_SELECTOR, EditorSelector.SINGLE_PAGE_EDITOR.value());
	}

}
