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

@Entity
@Table(name = "events_user_property")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "user_property_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "user_property_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "user_property_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "user_property_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "user_property_version")) })
public class UserProperty extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "user_property_parent_id", referencedColumnName = "user_id")
	private User owningUser;

	/**
	 * Data
	 */
	@Id
	@Column(name = "user_property_id")
	@GeneratedValue(generator = "events_user_property_id_seq")
	@TableGenerator(name = "events_user_property_id_seq", table = "events_sequence", initialValue = 20000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "user_property_key")
	private String key;

	@Basic
	@Column(name = "user_property_value")
	private String value;

	private UserProperty()
	{
		super();
	}

	private UserProperty(final User owningUser)
	{
		super();
		this.owningUser = owningUser;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getKey()
	{
		return key;
	}

	public User getOwningUser()
	{
		return owningUser;
	}

	public String getValue()
	{
		return stringValueOf(value);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setKey(final String key)
	{
		this.propertyChangeSupport.firePropertyChange("key", this.key, this.key = key);
	}

	public void setOwningUser(final User owningUser)
	{
		this.owningUser = owningUser;
	}

	public void setValue(final String value)
	{
		this.propertyChangeSupport.firePropertyChange("value", this.value, this.value = value);
	}

	public static UserProperty newInstance(final User owningUser)
	{
		return (UserProperty) AbstractEntity.newInstance(new UserProperty(owningUser));
	}

	public enum Property
	{
		BOOKING_CONFIRMATION_TEMPLATE_PATH, PARTICIPATION_CONFIRMATION_TEMPLATE_PATH, COURSE_INVITATION_TEMPLATE_PATH, DONATION_CONFIRMATION_TEMPLATE_PATH;

		public String key()
		{
			switch (this)
			{
				case BOOKING_CONFIRMATION_TEMPLATE_PATH:
				{
					return "booking.confirmation.template.path";
				}
				case PARTICIPATION_CONFIRMATION_TEMPLATE_PATH:
				{
					return "participation.confirmation.template.path";
				}
				case COURSE_INVITATION_TEMPLATE_PATH:
				{
					return "course.invitation.template.path";
				}
				case DONATION_CONFIRMATION_TEMPLATE_PATH:
				{
					return "donation.confirmation.template.path";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}
}
