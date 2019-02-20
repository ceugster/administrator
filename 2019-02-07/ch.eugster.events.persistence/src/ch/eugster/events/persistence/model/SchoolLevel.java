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
@Table(name = "events_school_level")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "school_level_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "school_level_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "school_level_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "school_level_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "school_level_version")) })
public class SchoolLevel extends AbstractEntity
{
	/**
	 * Data
	 */
	@Id
	@Column(name = "school_level_id")
	@GeneratedValue(generator = "events_school_level_id_seq")
	@TableGenerator(name = "events_school_level_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "school_level_name")
	private String name;

	private SchoolLevel()
	{
		super();
	}

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

	public void copy(SchoolLevel schoolClass)
	{
	}

	public void setName(String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name,
				this.name = name == null || name.isEmpty() ? null : name);
	}

	public String getName()
	{
		return name;
	}
	
	public static SchoolLevel newInstance()
	{
		return (SchoolLevel) AbstractEntity.newInstance(new SchoolLevel());
	}
}
