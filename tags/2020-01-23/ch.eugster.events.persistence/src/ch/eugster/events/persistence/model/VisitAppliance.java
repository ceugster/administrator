package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_visit_appliance")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "visit_appliance_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "visit_appliance_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "visit_appliance_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "visit_appliance_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "visit_appliance_version")) })
public class VisitAppliance extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "visit_appliance_visit_id", referencedColumnName = "visit_id")
	private Visit visit;

	@ManyToOne(optional = false)
	@JoinColumn(name = "visit_appliance_appliance_id", referencedColumnName = "appliance_id")
	private Appliance appliance;

	/**
	 * Data
	 */
	@Id
	@Column(name = "visit_appliance_id")
	@GeneratedValue(generator = "events_visit_appliance_id_seq")
	@TableGenerator(name = "events_visit_appliance_id_seq", table = "events_sequence")
	private Long id;

	private VisitAppliance()
	{
		super();
	}

	private VisitAppliance(final Visit visit, final Appliance appliance)
	{
		super();
		this.setVisit(visit);
		this.setAppliance(appliance);
	}

	public void copy(final VisitAppliance visitAppliance)
	{
	}

	public Appliance getAppliance()
	{
		return appliance;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public Visit getVisit()
	{
		return this.visit;
	}

	public void setAppliance(final Appliance appliance)
	{
		this.appliance = appliance;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setVisit(final Visit visit)
	{
		this.propertyChangeSupport.firePropertyChange("visit", this.visit, this.visit = visit);
	}

	public static VisitAppliance newInstance(final Visit visit, final Appliance appliance)
	{
		return (VisitAppliance) AbstractEntity.newInstance(new VisitAppliance(visit, appliance));
	}

}
