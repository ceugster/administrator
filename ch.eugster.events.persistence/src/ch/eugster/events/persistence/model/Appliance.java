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

@Entity
@Table(name = "events_appliance")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "appliance_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "appliance_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "appliance_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "appliance_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "appliance_version")) })
public class Appliance extends AbstractEntity
{
	@Id
	@Column(name = "appliance_id")
	@GeneratedValue(generator = "events_appliance_id_seq")
	@TableGenerator(name = "events_appliance_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "appliance_name")
	private String name;

	@Basic
	@Column(name = "appliance_description")
	private String description;

	@Override
	public Long getId()
	{
		return this.id;
	}

	@Override
	public void setId(Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

	public static Appliance newInstance()
	{
		return (Appliance) AbstractEntity.newInstance(new Appliance());
	}

}
