package ch.eugster.events.persistence.model;

import java.util.Calendar;
import java.util.Collection;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_season")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "season_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "season_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "season_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "season_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "season_version")) })
public class Season extends AbstractEntity
{
	@Transient
	public static final String IMAGE_CLOSED = "SEASON_CLOSED";

	@Transient
	public static final String IMAGE_ACTIVE = "SEASON_ACTIVE";

	@Id
	@Column(name = "season_id")
	@GeneratedValue(generator = "events_season_id_seq")
	@TableGenerator(name = "events_season_id_seq", table = "events_sequence", initialValue = 100, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "season_code")
	private String code;

	@Basic
	@Column(name = "season_title")
	private String title;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "season_start")
	private Calendar start;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "season_end")
	private Calendar end;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "season_closed")
	private boolean closed;

	/*
	 * Children
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "season")
	private Collection<Course> courses;

	private Season()
	{
	}

	public void addCourse(final Course course)
	{
		this.propertyChangeSupport.firePropertyChange("courses", this.courses, this.courses.add(course));
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public Collection<Course> getCourses()
	{
		return this.courses;
	}

	public Calendar getEnd()
	{
		return this.end;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getImageKey()
	{
		return this.isClosed() ? "SEASON_CLOSED" : "SEASON_ACTIVE";
	}

	public Calendar getStart()
	{
		return this.start;
	}

	public String getTitle()
	{
		return AbstractEntity.stringValueOf(this.title);
	}

	public boolean isClosed()
	{
		return this.closed;
	}

	public void removeCourse(final Course course)
	{
		this.propertyChangeSupport.firePropertyChange("courses", this.courses, this.courses.remove(course));
	}

	public void setClosed(final boolean closed)
	{
		this.propertyChangeSupport.firePropertyChange("closed", this.closed, this.closed = closed);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	public void setEnd(final Calendar end)
	{
		this.propertyChangeSupport.firePropertyChange("end", this.end, this.end = end);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setStart(final Calendar start)
	{
		this.propertyChangeSupport.firePropertyChange("start", this.start, this.start = start);
	}

	public void setTitle(final String title)
	{
		this.propertyChangeSupport.firePropertyChange("title", this.title, this.title = title);
	}

	public static Season newInstance()
	{
		return (Season) AbstractEntity.newInstance(new Season());
	}

}
