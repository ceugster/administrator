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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.queries.GlobalSettingsQuery;
import ch.eugster.events.persistence.service.ConnectionService;

@Entity
@Table(name = "events_global_settings")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "global_settings_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "global_settings_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "global_settings_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "global_settings_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "global_settings_version")) })
public class GlobalSettings extends AbstractEntity
{
	@Transient
	private static GlobalSettings instance;

	/**
	 * Kurse k�nnen Domänen festlegen
	 */
	@Id
	@Column(name = "global_settings_id")
	@GeneratedValue(generator = "events_global_settings_id_seq")
	@TableGenerator(name = "events_global_settings_id_seq", table = "events_sequence", allocationSize = 5, initialValue = 2)
	private Long id;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "global_settings_course_has_domain")
	private boolean courseHasDomain;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "global_settings_course_domain_mandatory")
	private boolean courseDomainMandatory;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "global_settings_course_has_category")
	private boolean courseHasCategory;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "global_settings_course_category_mandatory")
	private boolean courseCategoryMandatory;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "global_settings_course_has_rubric")
	private boolean courseHasRubric;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "global_settings_course_rubric_mandatory")
	private boolean courseRubricMandatory;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "global_settings_course_responsible_user_mandatory")
	private boolean courseResponsibleUserMandatory;

	@Basic
	@Column(name = "global_settings_booking_id_format")
	private String bookingIdFormat;

	@ManyToOne()
	@JoinColumn(name = "global_settings_default_country_id", referencedColumnName = "country_id")
	private Country country;

	@Override
	public Object clone()
	{
		GlobalSettings settings = GlobalSettings.newInstance();
		settings.setBookingIdFormat(this.getBookingIdFormat());
		settings.setCourseCategoryMandatory(this.isCourseCategoryMandatory());
		settings.setCourseDomainMandatory(this.isCourseDomainMandatory());
		settings.setCourseHasCategory(this.getCourseHasCategory());
		settings.setCourseHasDomain(this.getCourseHasDomain());
		settings.setCourseHasRubric(this.getCourseHasRubric());
		settings.setCourseResponsibleUserMandatory(this.isCourseResponsibleUserMandatory());
		settings.setCourseRubricMandatory(this.isCourseRubricMandatory());
		settings.setCountry(this.getCountry());
		return settings;
	}

	public String getBookingIdFormat()
	{
		return AbstractEntity.stringValueOf(this.bookingIdFormat);
	}

	public Country getCountry()
	{
		return country;
	}

	public boolean getCourseHasCategory()
	{
		return this.courseHasCategory;
	}

	public boolean getCourseHasDomain()
	{
		return this.courseHasDomain;
	}

	public boolean getCourseHasRubric()
	{
		return this.courseHasRubric;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public boolean isCourseCategoryMandatory()
	{
		return this.courseHasCategory && this.courseCategoryMandatory;
	}

	public boolean isCourseDomainMandatory()
	{
		return this.courseHasDomain && this.courseDomainMandatory;
	}

	public boolean isCourseResponsibleUserMandatory()
	{
		return this.courseResponsibleUserMandatory;
	}

	public boolean isCourseRubricMandatory()
	{
		return this.courseHasRubric && this.courseRubricMandatory;
	}

	public void setBookingIdFormat(final String bookingIdFormat)
	{
		this.propertyChangeSupport.firePropertyChange("bookingIdFormat", this.bookingIdFormat,
				this.bookingIdFormat = bookingIdFormat);
		this.bookingIdFormat = bookingIdFormat;
	}

	public void setCountry(final Country country)
	{
		this.country = country;
	}

	public void setCourseCategoryMandatory(final boolean courseCategoryMandatory)
	{
		this.propertyChangeSupport.firePropertyChange("courseCategoryMandatory", this.courseCategoryMandatory,
				this.courseCategoryMandatory = courseCategoryMandatory);
	}

	public void setCourseDomainMandatory(final boolean courseDomainMandatory)
	{
		this.propertyChangeSupport.firePropertyChange("courseDomainMandatory", this.courseDomainMandatory,
				this.courseDomainMandatory = courseDomainMandatory);
	}

	public void setCourseHasCategory(final boolean courseHasCategory)
	{
		this.propertyChangeSupport.firePropertyChange("courseHasCategory", this.courseHasCategory,
				this.courseHasCategory = courseHasCategory);
	}

	public void setCourseHasDomain(final boolean courseHasDomain)
	{
		this.propertyChangeSupport.firePropertyChange("courseHasDomain", this.courseHasDomain,
				this.courseHasDomain = courseHasDomain);
	}

	public void setCourseHasRubric(final boolean courseHasRubric)
	{
		this.propertyChangeSupport.firePropertyChange("courseHasRubric", this.courseHasRubric,
				this.courseHasRubric = courseHasRubric);
	}

	public void setCourseResponsibleUserMandatory(final boolean courseResponsibleUserMandatory)
	{
		this.propertyChangeSupport.firePropertyChange("courseResponsibleUserMandatory",
				this.courseResponsibleUserMandatory,
				this.courseResponsibleUserMandatory = courseResponsibleUserMandatory);
	}

	public void setCourseRubricMandatory(final boolean courseRubricMandatory)
	{
		this.propertyChangeSupport.firePropertyChange("courseRubricMandatory", this.courseRubricMandatory,
				this.courseRubricMandatory = courseRubricMandatory);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public static GlobalSettings getInstance()
	{
		if (GlobalSettings.instance == null)
		{
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
			tracker.open();
			try
			{
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					GlobalSettingsQuery query = (GlobalSettingsQuery) service.getQuery(GlobalSettings.class);
					instance = query.find(GlobalSettings.class, Long.valueOf(1L));
				}
			}
			finally
			{
				tracker.close();
			}
		}
		return GlobalSettings.instance;
	}

	public static GlobalSettings newInstance()
	{
		return (GlobalSettings) AbstractEntity.newInstance(new GlobalSettings());
	}

	public static void setInstance(final GlobalSettings settings)
	{
		instance = settings;
	}
}
