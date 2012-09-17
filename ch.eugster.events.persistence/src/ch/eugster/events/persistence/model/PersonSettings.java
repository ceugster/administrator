package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Customizer;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.queries.PersonSettingsQuery;
import ch.eugster.events.persistence.service.ConnectionService;

@Entity
@Table(name = "events_person_settings")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "person_settings_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "person_settings_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "person_settings_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "person_settings_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "person_settings_version")) })
@Customizer(DeletedFilter.class)
public class PersonSettings extends AbstractEntity
{
	@Transient
	private static PersonSettings instance;

	/**
	 * Kurse können Domänen festlegen
	 */
	@Id
	@Column(name = "person_settings_id")
	@GeneratedValue(generator = "events_person_settings_id_seq")
	@TableGenerator(name = "events_person_settings_id_seq", table = "events_sequence", allocationSize = 5, initialValue = 2)
	private Long id;

	/**
	 * Personenangaben
	 */
	@Basic
	@Column(name = "person_settings_editor_selector")
	private int editorSelector;

	@Basic
	@Column(name = "person_settings_editor_section_behaviour")
	private int editorSectionBehaviour;

	@Basic
	@Column(name = "person_settings_id_format")
	private String idFormat;

	@Basic
	@Column(name = "person_settings_person_label_format")
	private String personLabelFormat;

	@Basic
	@Column(name = "person_settings_address_label_format")
	private String addressLabelFormat;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "person_settings_person_has_domain")
	private boolean personHasDomain;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "person_settings_person_domain_mandatory")
	private boolean personDomainMandatory;

	@Override
	public Object clone()
	{
		PersonSettings settings = PersonSettings.newInstance();
		settings.setPersonDomainMandatory(this.isPersonDomainMandatory());
		settings.setPersonHasDomain(this.getPersonHasDomain());
		settings.setIdFormat(this.getIdFormat());
		settings.setPersonLabelFormat(this.getPersonLabelFormat());
		settings.setAddressLabelFormat(this.getAddressLabelFormat());
		return settings;
	}

	public String getAddressLabelFormat()
	{
		return stringValueOf(addressLabelFormat);
	}

	public int getEditorSectionBehaviour()
	{
		return editorSectionBehaviour;
	}

	public int getEditorSelector()
	{
		return editorSelector;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getIdFormat()
	{
		return AbstractEntity.stringValueOf(this.idFormat);
	}

	public boolean getPersonHasDomain()
	{
		return this.personHasDomain;
	}

	public String getPersonLabelFormat()
	{
		return stringValueOf(personLabelFormat);
	}

	public boolean isPersonDomainMandatory()
	{
		return this.personDomainMandatory;
	}

	public void setAddressLabelFormat(final String addressLabelFormat)
	{
		this.addressLabelFormat = addressLabelFormat;
	}

	public void setEditorSectionBehaviour(final int editorSectionBehaviour)
	{
		this.propertyChangeSupport.firePropertyChange("editorSectionBehaviour", this.editorSectionBehaviour,
				this.editorSectionBehaviour = editorSectionBehaviour);
	}

	public void setEditorSelector(final int editorSelector)
	{
		this.editorSelector = editorSelector;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setIdFormat(final String idFormat)
	{
		this.idFormat = idFormat;
	}

	public void setPersonDomainMandatory(final boolean personDomainMandatory)
	{
		this.personDomainMandatory = personDomainMandatory;
	}

	public void setPersonHasDomain(final boolean personHasDomain)
	{
		this.propertyChangeSupport.firePropertyChange("personHasDomain", this.personHasDomain,
				this.personHasDomain = personHasDomain);
	}

	public void setPersonLabelFormat(final String personLabelFormat)
	{
		this.propertyChangeSupport.firePropertyChange("personLabelFormat", this.personLabelFormat,
				this.personLabelFormat = personLabelFormat);
	}

	public static PersonSettings getInstance()
	{
		if (PersonSettings.instance == null)
		{
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				PersonSettingsQuery query = (PersonSettingsQuery) service.getQuery(PersonSettings.class);
				instance = query.find(PersonSettings.class, Long.valueOf(1L));
			}
			tracker.close();
		}
		return PersonSettings.instance;
	}

	public static PersonSettings newInstance()
	{
		return (PersonSettings) AbstractEntity.newInstance(new PersonSettings());
	}

	public static void setInstance(final PersonSettings settings)
	{
		instance = settings;
	}

}
