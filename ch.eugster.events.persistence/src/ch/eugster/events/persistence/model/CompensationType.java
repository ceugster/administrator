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
@Table(name = "events_compensation_type")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "compensation_type_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "compensation_type_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "compensation_type_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "compensation_type_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "compensation_type_version")) })
public class CompensationType extends AbstractEntity
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "compensation_type_id")
	@GeneratedValue(generator = "events_compensation_type_id_seq")
	@TableGenerator(name = "events_compensation_type_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "compensation_type_code")
	private String code;

	@Basic
	@Column(name = "compensation_type_name")
	protected String name;

	@Basic
	@Column(name = "compensation_type_desc")
	protected String description;

	public CompensationType()
	{
		super();
	}

	public void copy(final CompensationType source)
	{
		this.setCode(source.getCode());
		this.setDescription(source.getDescription());
		this.setName(source.getName());
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(this.name);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setName(final String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public static CompensationType newInstance()
	{
		return (CompensationType) AbstractEntity.newInstance(new CompensationType());
	}
}
