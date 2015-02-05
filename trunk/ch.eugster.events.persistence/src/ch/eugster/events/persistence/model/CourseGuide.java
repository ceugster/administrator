package ch.eugster.events.persistence.model;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_course_guide")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "course_guide_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "course_guide_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "course_guide_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "course_guide_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "course_guide_version")) })
public class CourseGuide extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "course_guide_course_id", referencedColumnName = "course_id")
	private Course course;

	@ManyToOne
	@JoinColumn(name = "course_guide_guide_id", referencedColumnName = "guide_id")
	private Guide guide;

	@ManyToOne
	@JoinColumn(name = "course_guide_guide_type_id", referencedColumnName = "guide_type_id")
	private GuideType guideType;

	@Id
	@Column(name = "course_guide_id")
	@GeneratedValue(generator = "events_course_guide_id_seq")
	@TableGenerator(name = "events_course_guide_id_seq", table = "events_sequence", initialValue = 20000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "course_guide_desc")
	private String description;

	@Basic
	@Column(name = "course_guide_phone")
	private String phone;

	@Basic
	@Column(name = "course_guide_note")
	private String note;

	/**
	 * Children
	 */
	@OneToMany(mappedBy = "courseGuide", cascade = CascadeType.ALL)
	private List<Compensation> compensations = new Vector<Compensation>();

	private CourseGuide()
	{
		super();
	}

	private CourseGuide(final Course course)
	{
		super();
		this.setCourse(course);
	}

	public void addCompensation(final Compensation compensation)
	{
		this.propertyChangeSupport.firePropertyChange("compensations", this.compensations,
				this.compensations.add(compensation));
	}

	public CourseGuide copy()
	{
		CourseGuide copy = CourseGuide.newInstance(this.getCourse());
		copy.setCourse(this.getCourse());
		copy.setDescription(this.getDescription());
		copy.setGuide(this.getGuide());
		copy.setGuideType(this.getGuideType());
		copy.setPhone(this.getPhone());
		copy.setCompensations(this.getCompensations());
		return copy;
	}

	public List<Compensation> getCompensations()
	{
		return this.compensations;
	}

	public Course getCourse()
	{
		return this.course;
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	public Guide getGuide()
	{
		return this.guide;
	}

	public GuideType getGuideType()
	{
		return this.guideType;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getPhone()
	{
		return (this.phone == null || this.phone.isEmpty()) ? this.getGuide().getPhone() : this.phone;
	}

	public void removeCompensation(final Compensation compensation)
	{
		this.propertyChangeSupport.firePropertyChange("compensations", this.compensations,
				this.compensations.remove(compensation));
	}

	public void setCompensations(final List<Compensation> compensations)
	{
		this.propertyChangeSupport.firePropertyChange("compensations", this.compensations,
				this.compensations = compensations);
	}

	public void setCourse(final Course course)
	{
		this.propertyChangeSupport.firePropertyChange("course", this.course, this.course = course);
	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
	}

	public void setGuide(final Guide guide)
	{
		this.propertyChangeSupport.firePropertyChange("guide", this.guide, this.guide = guide);
	}

	public void setGuideType(final GuideType guideType)
	{
		this.propertyChangeSupport.firePropertyChange("guideType", this.guideType, this.guideType = guideType);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setPhone(final String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = phone);
	}

	public void setNote(String note) 
	{
		this.propertyChangeSupport.firePropertyChange("note", this.note, this.note = note);
	}

	public String getNote() 
	{
		return AbstractEntity.stringValueOf(this.note);
	}
	
	public String getEmail()
	{
		LinkPersonAddress link = this.guide.getLink();
		if (!link.getPerson().getEmail().isEmpty())
		{
			return link.getPerson().getEmail();
		}
		if (!link.getAddress().getEmail().isEmpty())
		{
			return link.getAddress().getEmail();
		}
		if (!link.getEmail().isEmpty())
		{
			return link.getEmail();
		}
		return "";
	}

	public static CourseGuide newInstance(final Course course)
	{
		return (CourseGuide) AbstractEntity.newInstance(new CourseGuide(course));
	}
}
