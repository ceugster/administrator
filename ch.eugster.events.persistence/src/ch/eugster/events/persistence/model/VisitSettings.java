package ch.eugster.events.persistence.model;

import java.util.Calendar;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "events_visit_settings")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "visit_settings_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "visit_settings_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "visit_settings_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "visit_settings_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "visit_settings_version")) })
public class VisitSettings extends AbstractEntity
{
	@OneToOne
	@JoinColumn(name = "visit_settings_default_address_type_id", referencedColumnName = "address_type_id")
	private AddressType defaultAddressType;

	@Id
	@Column(name = "visit_settings_id")
	@GeneratedValue(generator = "events_visit_settings_id_seq")
	@TableGenerator(name = "events_visit_settings_id_seq", table = "events_sequence", allocationSize = 5, initialValue = 2)
	private Long id;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "visit_settings_start_range")
	private Calendar startRange;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "visit_settings_end_range")
	private Calendar endRange;

	public AddressType getDefaultAddressType()
	{
		return defaultAddressType;
	}

	public Calendar getEndRange()
	{
		return endRange;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public Calendar getStartRange()
	{
		return startRange;
	}

	public void setDefaultAddressType(final AddressType defaultAddressType)
	{
		this.defaultAddressType = defaultAddressType;
	}

	public void setEndRange(final Calendar endRange)
	{
		this.endRange = endRange;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setStartRange(final Calendar startRange)
	{
		this.startRange = startRange;
	}

}
