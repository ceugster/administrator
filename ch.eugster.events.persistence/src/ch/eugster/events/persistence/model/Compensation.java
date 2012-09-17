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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_compensation")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "compensation_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "compensation_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "compensation_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "compensation_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "compensation_version")) })
@Customizer(DeletedFilter.class)
public class Compensation extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "compensation_compensation_type_id", referencedColumnName = "compensation_type_id")
	private CompensationType compensationType;

	@ManyToOne
	@JoinColumn(name = "compensation_course_guide_id", referencedColumnName = "course_guide_id")
	private CourseGuide courseGuide;

	/**
	 * Data
	 */
	@Id
	@Column(name = "compensation_id")
	@GeneratedValue(generator = "events_compensation_id_seq")
	@TableGenerator(name = "events_compensation_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "compensation_amount")
	private double amount;

	public Compensation()
	{
		super();
	}

	public Compensation(final CourseGuide courseGuide)
	{
		super();
		this.setCourseGuide(courseGuide);
	}

	public void copy(final Compensation source)
	{
		this.setAmount(source.getAmount());
		this.setCompensationType(source.getCompensationType());
		this.setCourseGuide(source.getCourseGuide());
	}

	public double getAmount()
	{
		return this.amount;
	}

	public CompensationType getCompensationType()
	{
		return this.compensationType;
	}

	public CourseGuide getCourseGuide()
	{
		return this.courseGuide;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public void setAmount(final double amount)
	{
		this.propertyChangeSupport.firePropertyChange("amount", this.amount, this.amount = amount);
	}

	public void setCompensationType(final CompensationType compensationType)
	{
		this.propertyChangeSupport.firePropertyChange("compensationType", this.compensationType,
				this.compensationType = compensationType);
	}

	public void setCourseGuide(final CourseGuide courseGuide)
	{
		this.propertyChangeSupport.firePropertyChange("courseGuide", this.courseGuide, this.courseGuide = courseGuide);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public static Compensation newInstance()
	{
		return (Compensation) AbstractEntity.newInstance(new Compensation());
	}

	public static Compensation newInstance(final CourseGuide courseGuide)
	{
		return (Compensation) AbstractEntity.newInstance(new Compensation(courseGuide));
	}

}
