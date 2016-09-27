package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "events_visit")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "visit_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "visit_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "visit_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "visit_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "visit_version")) })
public class Visit extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "visit_visit_theme_id", referencedColumnName = "visit_theme_id")
	private VisitTheme theme;

	@ManyToOne(optional = false)
	@JoinColumn(name = "visit_teacher_id", referencedColumnName = "teacher_id")
	private Teacher teacher;

	@ManyToOne(optional = false)
	@JoinColumn(name = "visit_school_class_id", referencedColumnName = "school_class_id")
	private SchoolClass schoolClass;

	/**
	 * Data
	 */
	@Id
	@Column(name = "visit_id")
	@GeneratedValue(generator = "events_visit_id_seq")
	@TableGenerator(name = "events_visit_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "visit_best_reach_time")
	private String bestReachTime;

	@Basic
	@Column(name = "visit_state")
	@Enumerated(EnumType.ORDINAL)
	private State state;

	@Basic
	@Column(name = "visit_selected_phone")
	@Enumerated(EnumType.ORDINAL)
	private SelectedPhone selectedPhone;

	@Basic
	@Column(name = "visit_selected_email")
	@Enumerated(EnumType.ORDINAL)
	private SelectedEmail selectedEmail;

	@Basic
	@Column(name = "visit_class_name")
	private String className;

	@Basic
	@Column(name = "visit_pupils")
	private int pupils;

	@Basic
	@Column(name = "visit_floor")
	private String floor;

	@Basic
	@Column(name = "visit_class_room")
	private String classRoom;

	@Basic
	@Column(name = "visit_color")
	private int color;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "visit_start")
	private Calendar start;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "visit_end")
	private Calendar end;

	@OneToMany(mappedBy = "visit", cascade = ALL)
	private List<VisitAppliance> appliances = new Vector<VisitAppliance>();

	@OneToMany(mappedBy = "visit", cascade = ALL)
	private List<VisitVisitor> visitors = new Vector<VisitVisitor>();

	private Visit()
	{
		super();
	}

	public void addAppliance(final VisitAppliance appliance)
	{
		this.propertyChangeSupport.firePropertyChange("addAppliance", this.appliances, this.appliances.add(appliance));
	}

	public void addVisitor(final VisitVisitor visitor)
	{
		this.propertyChangeSupport.firePropertyChange("visitors", this.visitors, this.visitors.add(visitor));
	}

	public List<VisitAppliance> getAppliances()
	{
		return appliances;
	}

	public String getBestReachTime()
	{
		return bestReachTime;
	}

	public String getClassName()
	{
		return stringValueOf(className);
	}

	public String getClassRoom()
	{
		return classRoom;
	}

	public Calendar getEnd()
	{
		return end;
	}

	public String getFloor()
	{
		return floor;
	}

	public String getFormattedPeriod()
	{
		StringBuilder period = new StringBuilder();
		if (start != null)
		{
			period = period.append(SimpleDateFormat.getDateInstance().format(this.getStart().getTime())).append(" ")
					.append(SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(this.getStart().getTime()));

			if (end != null)
			{
				period = period.append(" - ");
				if (start.get(Calendar.YEAR) != end.get(Calendar.YEAR)
						|| start.get(Calendar.MONTH) != end.get(Calendar.MONTH)
						|| start.get(Calendar.DATE) != end.get(Calendar.DATE))
				{
					period = period.append(SimpleDateFormat.getDateInstance().format(this.getEnd().getTime())).append(
							" ");
				}
				period = period.append(SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(
						this.getEnd().getTime()));
			}
		}
		return period.toString();
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public int getPupils()
	{
		return pupils;
	}

	public SchoolClass getSchoolClass()
	{
		return schoolClass;
	}

	public SelectedEmail getSelectedEmail()
	{
		return selectedEmail;
	}

	public SelectedPhone getSelectedPhone()
	{
		return selectedPhone;
	}

	public Calendar getStart()
	{
		return start;
	}

	public State getState()
	{
		return state;
	}

	public Teacher getTeacher()
	{
		return teacher;
	}

	public VisitTheme getTheme()
	{
		return theme;
	}

	public int getColor()
	{
		return this.color;
	}
	
	public List<VisitVisitor> getVisitors()
	{
		return this.visitors;
	}

	public List<VisitVisitor> getValidVisitors()
	{
		List<VisitVisitor> visitors = new ArrayList<VisitVisitor>();
		for (VisitVisitor visitor : this.visitors)
		{
			if (visitor.isValid())
			{
				visitors.add(visitor);
			}
		}
		return visitors;
	}

	public void removeAppliance(final VisitAppliance appliance)
	{
		this.propertyChangeSupport.firePropertyChange("removeAppliance", this.appliances,
				this.appliances.remove(appliance));
	}

	public void removeVisitor(final VisitVisitor visitor)
	{
		this.propertyChangeSupport.firePropertyChange("visitors", this.visitors, this.visitors.remove(visitor));
	}

	public void setBestReachTime(final String bestReachTime)
	{
		this.propertyChangeSupport.firePropertyChange("bestReachTime", this.bestReachTime,
				this.bestReachTime = bestReachTime == null || bestReachTime.isEmpty() ? null : bestReachTime);
	}

	public void setClassName(final String className)
	{
		this.propertyChangeSupport.firePropertyChange("className", this.className, this.className = className);
	}

	public void setClassRoom(final String classRoom)
	{
		this.propertyChangeSupport.firePropertyChange("classRoom", this.classRoom, this.classRoom = classRoom);
	}

	public void setEnd(final Calendar end)
	{
		this.propertyChangeSupport.firePropertyChange("end", this.end, this.end = end);
		this.end = end;
	}

	public void setFloor(final String floor)
	{
		this.propertyChangeSupport.firePropertyChange("floor", this.floor, this.floor = floor);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setPupils(final int pupils)
	{
		this.propertyChangeSupport.firePropertyChange("pupils", this.pupils, this.pupils = pupils);
	}

	public void setSchoolClass(final SchoolClass schoolClass)
	{
		this.propertyChangeSupport.firePropertyChange("schoolClass", this.schoolClass, this.schoolClass = schoolClass);
	}

	public void setColor(final int color)
	{
		this.propertyChangeSupport.firePropertyChange("color", this.color, this.color = color);
	}

	public void setSelectedEmail(final SelectedEmail selectedEmail)
	{
		this.propertyChangeSupport.firePropertyChange("selectedEmail", this.selectedEmail,
				this.selectedEmail = selectedEmail);
	}

	public void setSelectedPhone(final SelectedPhone phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.selectedPhone,
				this.selectedPhone = phone == null ? SelectedPhone.NONE : phone);
	}

	public void setStart(final Calendar start)
	{
		this.propertyChangeSupport.firePropertyChange("start", this.start, this.start = start);
	}

	public void setState(final State state)
	{
		this.propertyChangeSupport.firePropertyChange("state", this.state, this.state = state);
	}

	public void setTeacher(final Teacher teacher)
	{
		this.propertyChangeSupport.firePropertyChange("teacher", this.teacher, this.teacher = teacher);
	}

	public void setTheme(final VisitTheme theme)
	{
		this.propertyChangeSupport.firePropertyChange("theme", this.theme, this.theme = theme);
	}

	public void setDeleted(boolean deleted)
	{
		for (VisitVisitor visitorVisit : visitors)
		{
			if (visitorVisit.isDeleted() != deleted)
			{
				visitorVisit.setDeleted(deleted);
			}
		}
		super.setDeleted(deleted);
	}

	public static Visit newInstance()
	{
		return (Visit) AbstractEntity.newInstance(new Visit());
	}

	public enum State
	{
		PROVISORILY, DEFINITIVE;

		public String label()
		{
			switch (this)
			{
				case PROVISORILY:
				{
					return "Provisorische Anmeldung";
				}
				case DEFINITIVE:
				{
					return "Definitive Anmeldung";
				}
				default:
				{
					return "";
				}
			}
		}
	}
}
