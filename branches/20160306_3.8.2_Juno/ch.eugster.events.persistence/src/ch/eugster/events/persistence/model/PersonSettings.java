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

	@Basic
	@Convert("booleanConverter")
	@Column(name = "person_settings_add_blank_after_point_in_city")
	private boolean addBlankAfterPointInCity;

	@Basic
	@Column(name = "person_settings_criteria_min_length")
	private int criteriaMinLength;

	@Basic
	@Column(name = "person_settings_max_records_listed")
	private int maxRecordsListed;

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

	public boolean isAddBlankAfterPointInCity()
	{
		return addBlankAfterPointInCity;
	}

	public int getCriteriaMinLength()
	{
		return this.criteriaMinLength == 0 ? 3 : this.criteriaMinLength;
	}

	public int getMaxRecordListed()
	{
		return this.maxRecordsListed;
	}

	public boolean isPersonDomainMandatory()
	{
		return this.personDomainMandatory;
	}

	public void setAddBlankAfterPointInCity(final boolean addBlankAfterPointInCity)
	{
		this.addBlankAfterPointInCity = addBlankAfterPointInCity;
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

	public void setCriteriaMinLength(final int criteriaMinLength)
	{
		this.propertyChangeSupport.firePropertyChange("criteriaMinLength", this.criteriaMinLength,
				this.criteriaMinLength = criteriaMinLength);
	}

	public void setMaxRecordsListed(final int maxRecordsListed)
	{
		this.propertyChangeSupport.firePropertyChange("maxRecordsListed", this.maxRecordsListed,
				this.maxRecordsListed = maxRecordsListed);
	}

	public static PersonSettings getInstance()
	{
		if (PersonSettings.instance == null)
		{
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
			tracker.open();
			try
			{
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					PersonSettingsQuery query = (PersonSettingsQuery) service.getQuery(PersonSettings.class);
					PersonSettings.instance = query.find(PersonSettings.class, Long.valueOf(1L));
				}
			}
			finally
			{
				tracker.close();
			}
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
