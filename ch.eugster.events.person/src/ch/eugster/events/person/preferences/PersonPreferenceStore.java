package ch.eugster.events.person.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.queries.PersonSettingsQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class PersonPreferenceStore extends ScopedPreferenceStore
{
	private static PersonPreferenceStore store;

	private PersonPreferenceStore()
	{
		super(new InstanceScope(), Activator.PLUGIN_ID);
		this.setValue(PreferenceInitializer.KEY_ID_FORMAT, PersonSettings.getInstance().getIdFormat());
		this.setValue(PreferenceInitializer.KEY_PERSON_LABEL_FORMAT, PersonFormatter.getInstance()
				.convertPersonLabelToVisible(PersonSettings.getInstance().getPersonLabelFormat()));
		this.setValue(PreferenceInitializer.KEY_ADDRESS_LABEL_FORMAT, AddressFormatter.getInstance()
				.convertAddressLabelToVisible(PersonSettings.getInstance().getAddressLabelFormat()));
		this.setValue(PreferenceInitializer.KEY_USE_DOMAIN,
				Boolean.toString(PersonSettings.getInstance().getPersonHasDomain()));
		this.setValue(PreferenceInitializer.KEY_DOMAIN_MANDATORY,
				Boolean.toString(PersonSettings.getInstance().isPersonDomainMandatory()));
		this.setValue(PreferenceInitializer.KEY_EDITOR_SELECTOR,
				Integer.toString(PersonSettings.getInstance().getEditorSelector()));
		this.setValue(PreferenceInitializer.KEY_EDITOR_SECTION_BEHAVIOUR,
				Integer.toString(PersonSettings.getInstance().getEditorSectionBehaviour()));
		this.setValue(PreferenceInitializer.KEY_EDITOR_ADD_BLANK_AFTER_DOT_IN_CITY,
				Boolean.toString(PersonSettings.getInstance().isAddBlankAfterPointInCity()));
	}

	@Override
	public void save()
	{
		if (this.needsSaving())
		{
			PersonSettings.getInstance().setIdFormat(this.getString(PreferenceInitializer.KEY_ID_FORMAT));
			PersonSettings.getInstance().setPersonLabelFormat(
					PersonFormatter.getInstance().convertPersonLabelToStored(
							this.getString(PreferenceInitializer.KEY_PERSON_LABEL_FORMAT)));
			PersonSettings.getInstance().setAddressLabelFormat(
					AddressFormatter.getInstance().convertAddressLabelToStored(
							this.getString(PreferenceInitializer.KEY_ADDRESS_LABEL_FORMAT)));
			PersonSettings.getInstance().setPersonHasDomain(this.getBoolean(PreferenceInitializer.KEY_USE_DOMAIN));
			PersonSettings.getInstance().setPersonDomainMandatory(
					this.getBoolean(PreferenceInitializer.KEY_DOMAIN_MANDATORY));
			PersonSettings.getInstance().setEditorSelector(this.getInt(PreferenceInitializer.KEY_EDITOR_SELECTOR));
			PersonSettings.getInstance().setEditorSectionBehaviour(
					this.getInt(PreferenceInitializer.KEY_EDITOR_SECTION_BEHAVIOUR));
			PersonSettings.getInstance().setAddBlankAfterPointInCity(
					this.getBoolean(PreferenceInitializer.KEY_EDITOR_ADD_BLANK_AFTER_DOT_IN_CITY));
			try
			{
				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ConnectionService.class.getName(), null);
				tracker.open();
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					PersonSettingsQuery query = (PersonSettingsQuery) service.getQuery(PersonSettings.class);
					PersonSettings.setInstance(query.merge(PersonSettings.getInstance()));
				}

				super.save();
			}
			catch (Exception e)
			{
			}
		}
	}

	public static PersonPreferenceStore getInstance()
	{
		if (store == null)
		{
			store = new PersonPreferenceStore();
		}
		return store;
	}

}
