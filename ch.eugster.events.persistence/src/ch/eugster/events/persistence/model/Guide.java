package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

import java.util.List;
import java.util.Vector;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;

@Entity
@Table(name = "events_guide")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "guide_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "guide_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "guide_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "guide_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "guide_version")) })
public class Guide extends AbstractEntity
{
	/**
	 * References
	 */
	@OneToOne(optional = false)
	@JoinColumn(name = "guide_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	@Id
	@Column(name = "guide_id")
	@GeneratedValue(generator = "events_guide_id_seq")
	@TableGenerator(name = "events_guide_id_seq", table = "events_sequence", initialValue = 50000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "guide_desc")
	private String description;

	@Basic
	@Column(name = "guide_phone")
	private String phone;

	@OneToMany(mappedBy = "guide", cascade = ALL)
	private List<CourseGuide> courseGuides = new Vector<CourseGuide>();

	private Guide()
	{
		super();
	}

	private Guide(final LinkPersonAddress link)
	{
		super();
		this.setLink(link);
	}

	public boolean isValid()
	{
		return !this.deleted && this.getLink().isValid();
	}
	
	public void addCourseGuide(final CourseGuide courseGuide)
	{
		this.propertyChangeSupport.firePropertyChange("courseGuides", this.courseGuides,
				this.courseGuides.add(courseGuide));
	}

	public List<CourseGuide> getCourseGuides()
	{
		return this.courseGuides;
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

	public LinkPersonAddress getLink()
	{
		return this.link;
	}

	public String getPhone()
	{
		if (this.phone == null || this.phone.isEmpty())
		{
			if (this.getLink().getPerson().getPhone().isEmpty())
			{
				if (this.getLink().getAddress().getPhone().isEmpty())
				{
					return this.getLink().getPhone();
				}
				else
				{
					return this.getLink().getAddress().getPhone();
				}
			}
			else
			{
				return this.getLink().getPerson().getPhone();
			}
		}
		else
		{
			return this.phone;
		}
	}
	
	public String getFormattedPhone()
	{
		if (this.getLink().getPerson().getPhone().isEmpty())
		{
			if (this.getLink().getAddress().getPhone().isEmpty())
			{
				return LinkPersonAddressFormatter.getInstance().formatPhoneWithOptionalPrefix(this.getLink().getAddress().getCountry(), this.getLink().getPhone());
			}
			else
			{
				return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(this.getLink().getAddress().getCountry(), this.getLink().getAddress().getPhone());
			}
		}
		else
		{
			return PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(this.getLink().getPerson().getCountry(), this.getLink().getPerson().getPhone());
		}
	}

	public void removeCourseGuide(final CourseGuide courseGuide)
	{
		this.propertyChangeSupport.firePropertyChange("courseGuides", this.courseGuides,
				this.courseGuides.remove(courseGuide));
	}

	public void setCourseGuides(final List<CourseGuide> courseGuides)
	{
		this.propertyChangeSupport.firePropertyChange("courseGuides", this.courseGuides,
				this.courseGuides = courseGuides);
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		super.setDeleted(deleted);
		for (CourseGuide courseGuide : this.courseGuides)
			courseGuide.setDeleted(deleted);
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

	public void setLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("link", this.link, this.link = link);
	}

	public void setPhone(final String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = phone);
	}

	public static Guide newInstance(final LinkPersonAddress link)
	{
		return (Guide) AbstractEntity.newInstance(new Guide(link));
	}

	// public static Guide getByPerson(LinkPersonAddress link)
	// {
	// Expression expression = new
	// ExpressionBuilder(Guide.class).get("link").equal(link);
	// return (Guide) AbstractEntity.server.get(Guide.class, expression);
	// }

}
