package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_school_class")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "school_class_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "school_class_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "school_class_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "school_class_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "school_class_version")) })
public class SchoolClass extends AbstractEntity
{
	/**
	 * Data
	 */
	@Id
	@Column(name = "school_class_id")
	@GeneratedValue(generator = "events_school_class_id_seq")
	@TableGenerator(name = "events_school_class_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "school_class_name")
	private String name;

	@Basic
	@Column(name = "school_class_level")
	@Enumerated(EnumType.ORDINAL)
	private Level level;

	private SchoolClass()
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

	public void copy(SchoolClass schoolClass)
	{
	}

	public static SchoolClass newInstance()
	{
		return (SchoolClass) AbstractEntity.newInstance(new SchoolClass());
	}

	public void setLevel(Level level)
	{
		this.propertyChangeSupport.firePropertyChange("level", this.level, this.level = level);
	}

	public Level getLevel()
	{
		return this.level;
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

	public enum Level
	{
		BASIC, LOWER, MIDDLE, UPPER;

		public String label()
		{
			switch (this)
			{
				case BASIC:
				{
					return "Kindergarten";
				}
				case LOWER:
				{
					return "Unterstufe";
				}
				case MIDDLE:
				{
					return "Mittelstufe";
				}
				case UPPER:
				{
					return "Oberstufe";
				}
				default:
				{
					return "";
				}
			}
		}
	}
}
