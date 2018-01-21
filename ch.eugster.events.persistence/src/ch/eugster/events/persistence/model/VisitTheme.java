package ch.eugster.events.persistence.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_visit_theme")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "visit_theme_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "visit_theme_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "visit_theme_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "visit_theme_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "visit_theme_version")) })
public class VisitTheme extends AbstractEntity
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "visit_theme_id")
	@GeneratedValue(generator = "events_visit_theme_id_seq")
	@TableGenerator(name = "events_visit_theme_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "visit_theme_name")
	private String name;

	@Basic
	@Column(name = "visit_theme_description")
	private String description;

	@Basic
	@Column(name = "visit_theme_color")
	private Integer color;

	@Basic
	@Column(name = "visit_theme_hidden")
	@Convert("booleanConverter")
	private boolean hidden;

	/*
	 * Children
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "theme")
	private List<Visit> visits = new Vector<Visit>();

	private VisitTheme()
	{
		super();
	}

	private VisitTheme(final LinkPersonAddress link)
	{
		super();
	}

	public void addVisit(final Visit visit)
	{
		this.propertyChangeSupport.firePropertyChange("visits", this.visits, this.visits.add(visit));
	}

	public void copy(final VisitTheme source)
	{
	}

	public Integer getColor()
	{
		return color;
	}

	public String getDescription()
	{
		return stringValueOf(description);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(name);
	}

	public List<Visit> getVisits(boolean deletedToo)
	{
		List<Visit> validVisits = new ArrayList<Visit>();
		for (Visit visit : this.visits)
		{
			if (deletedToo || !visit.isDeleted())
			{
				validVisits.add(visit);
			}
		}
		return validVisits;
	}
	
	public boolean isHidden()
	{
		return this.hidden;
	}

	public void removeVisit(final Visit visit)
	{
		this.propertyChangeSupport.firePropertyChange("visits", this.visits, this.visits.remove(visit));
	}

	public void setColor(final Integer color)
	{
		this.propertyChangeSupport.firePropertyChange("color", this.color, this.color = color);
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

	public void setVisits(final List<Visit> visits)
	{
		this.propertyChangeSupport.firePropertyChange("visits", this.visits, this.visits = visits);
	}
	
	public void setHidden(boolean hidden)
	{
		this.propertyChangeSupport.firePropertyChange("hidden", this.hidden, this.hidden = hidden);
	}

	public static VisitTheme newInstance()
	{
		return (VisitTheme) AbstractEntity.newInstance(new VisitTheme());
	}
}
