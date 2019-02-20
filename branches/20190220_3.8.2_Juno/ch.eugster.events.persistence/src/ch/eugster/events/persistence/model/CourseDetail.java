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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_course_detail")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "course_detail_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "course_detail_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "course_detail_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "course_detail_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "course_detail_version")) })
public class CourseDetail extends AbstractEntity implements Comparable<CourseDetail>
{
	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "course_detail_course_id", referencedColumnName = "course_id")
	private Course course;

	/**
	 * Data
	 */
	@Id
	@Column(name = "course_detail_id")
	@GeneratedValue(generator = "events_course_detail_id_seq")
	@TableGenerator(name = "events_course_detail_id_seq", table = "events_sequence", initialValue = 2000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "course_detail_location")
	private String location;

	@Basic
	@Column(name = "course_detail_meeting_point")
	private String meetingPoint;

	@Basic
	@Column(name = "course_detail_journey")
	private String journey;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_detail_start")
	private Calendar start;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_detail_end")
	private Calendar end;

	@Basic
	@Column(name = "course_detail_with_substitute_date")
	@Convert("booleanConverter")
	private boolean withSubstituteDate;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_detail_substitute_start")
	private Calendar substituteStart;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_detail_substitute_end")
	private Calendar substituteEnd;

	private CourseDetail()
	{
		super();
	}

	private CourseDetail(final Course course)
	{
		super();
		this.setCourse(course);
	}

	@Override
	public int compareTo(final CourseDetail other)
	{
		Calendar thisStart = this.isWithSubstituteDate() ? this.getSubstituteStart() : this.getStart();
		Calendar otherStart = other.isWithSubstituteDate() ? other.getSubstituteStart() : other.getStart();
		if (thisStart == null && otherStart == null)
		{
			return 0;
		}
		else if (thisStart != null && otherStart != null)
		{
			return thisStart.getTime().compareTo(otherStart.getTime());
		}
		else if (thisStart == null)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}

	public CourseDetail copy(Course course)
	{
		CourseDetail copy = CourseDetail.newInstance(course);
		copy.setEnd(this.getEnd());
		copy.setJourney(this.getJourney());
		copy.setLocation(this.getLocation());
		copy.setMeetingPoint(this.getMeetingPoint());
		copy.setStart(this.getStart());
		copy.setSubstituteEnd(this.getSubstituteEnd());
		copy.setSubstituteStart(this.getSubstituteStart());
		copy.setWithSubstituteDate(this.isWithSubstituteDate());
		return copy;
	}

	public Course getCourse()
	{
		return this.course;
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

	public String getJourney()
	{
		return AbstractEntity.stringValueOf(this.journey);
	}

	public String getLocation()
	{
		return AbstractEntity.stringValueOf(this.location);
	}

	public String getMeetingPoint()
	{
		return AbstractEntity.stringValueOf(this.meetingPoint);
	}

	public Calendar getStart()
	{
		return this.start;
	}

	public Calendar getSubstituteEnd()
	{
		return this.substituteEnd;
	}

	public Calendar getSubstituteStart()
	{
		return this.substituteStart;
	}

	public boolean isWithSubstituteDate()
	{
		return this.withSubstituteDate;
	}

	public void setCourse(final Course course)
	{
		this.propertyChangeSupport.firePropertyChange("course", this.course, this.course = course);
		this.course = course;
	}

	public void setEnd(final Calendar end)
	{
		this.propertyChangeSupport.firePropertyChange("end", this.end, this.end = end);
	}

	@Override
	public void setId(final Long id)
	{
		this.id = id;
	}

	public void setJourney(final String journey)
	{
		this.propertyChangeSupport.firePropertyChange("journey", this.journey, this.journey = journey);
		this.journey = journey;
	}

	public void setLocation(final String location)
	{
		this.propertyChangeSupport.firePropertyChange("location", this.location, this.location = location);
	}

	public void setMeetingPoint(final String meetingPoint)
	{
		this.propertyChangeSupport.firePropertyChange("meetingPoint", this.meetingPoint,
				this.meetingPoint = meetingPoint);
		this.meetingPoint = meetingPoint;
	}

	public void setStart(final Calendar start)
	{
		this.propertyChangeSupport.firePropertyChange("start", this.start, this.start = start);
		this.start = start;
	}

	public void setSubstituteEnd(final Calendar substituteEnd)
	{
		this.propertyChangeSupport.firePropertyChange("substituteEnd", this.substituteEnd,
				this.substituteEnd = substituteEnd);
		this.substituteEnd = substituteEnd;
	}

	public void setSubstituteStart(final Calendar substituteStart)
	{
		this.propertyChangeSupport.firePropertyChange("substituteStart", this.substituteStart,
				this.substituteStart = substituteStart);
		this.substituteStart = substituteStart;
	}

	public void setWithSubstituteDate(final boolean withSubstituteDate)
	{
		this.propertyChangeSupport.firePropertyChange("withSubstituteDate", this.withSubstituteDate,
				this.withSubstituteDate = withSubstituteDate);
		this.withSubstituteDate = withSubstituteDate;
	}

	public static CourseDetail getNewInstance(final Course course)
	{
		return new CourseDetail(course);
	}

	public static CourseDetail newInstance(final Course course)
	{
		return (CourseDetail) AbstractEntity.newInstance(new CourseDetail(course));
	}

}
