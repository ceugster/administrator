package ch.eugster.events.persistence.model;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_booking_type")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "booking_type_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "booking_type_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "booking_type_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "booking_type_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "booking_type_version")) })
@Customizer(DeletedFilter.class)
public class BookingType extends AbstractEntity
{

	@Transient
	private static NumberFormat nf;

	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "booking_type_course_id", referencedColumnName = "course_id")
	private Course course;

	@OneToOne(optional = true)
	@JoinColumn(name = "booking_type_membership_id", referencedColumnName = "membership_id")
	private Membership membership;

	/**
	 * Data
	 */
	@Id
	@Column(name = "booking_type_id")
	@GeneratedValue(generator = "events_booking_type_id_seq")
	@TableGenerator(name = "events_booking_type_id_seq", table = "events_sequence", initialValue = 10000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "booking_type_code")
	private String code;

	@Basic
	@Column(name = "booking_type_name")
	private String name;

	@Basic
	@Column(name = "booking_type_price")
	private double price;

	@Basic
	@Column(name = "booking_type_max_age")
	private int maxAge;

	@Basic
	@Column(name = "booking_type_annulation_charges")
	private double annulationCharges;

	private BookingType()
	{
		super();
	}

	private BookingType(final Course course)
	{
		super();
		this.setCourse(course);
	}

	public double getAnnulationCharges()
	{
		return this.annulationCharges;
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getComboLabel()
	{
		StringBuilder builder = new StringBuilder();
		if (this.getCode().trim().length() > 0)
		{
			builder = builder.append(this.getCode());
			builder = builder.append(" - ");
		}
		if (this.getName().trim().length() > 0)
		{
			builder = builder.append(this.getName());
			builder = builder.append(" - ");
		}
		builder = builder.append("Betrag " + this.getNumberFormat().format(this.getPrice()));
		return builder.toString();
	}

	public Course getCourse()
	{
		return this.course;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public int getMaxAge()
	{
		return maxAge;
	}

	public Membership getMembership()
	{
		return membership;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(this.name);
	}

	private NumberFormat getNumberFormat()
	{
		if (nf == null)
		{
			nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
			nf.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
		}
		return nf;
	}

	public double getPrice()
	{
		return this.price;
	}

	public void setAnnulationCharges(final double annulationCharges)
	{
		this.propertyChangeSupport.firePropertyChange("annulationCharges", this.annulationCharges,
				this.annulationCharges = annulationCharges);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	public void setCourse(final Course course)
	{
		this.propertyChangeSupport.firePropertyChange("course", this.course, this.course = course);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setMaxAge(final int maxAge)
	{
		this.propertyChangeSupport.firePropertyChange("maxAge", this.maxAge, this.maxAge = maxAge);
	}

	public void setMembership(final Membership membership)
	{
		this.propertyChangeSupport.firePropertyChange("membership", this.membership, this.membership = membership);
	}

	public void setName(final String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public void setPrice(final double price)
	{
		this.propertyChangeSupport.firePropertyChange("price", this.price, this.price = price);
	}

	public static BookingType newInstance()
	{
		return (BookingType) AbstractEntity.newInstance(new BookingType());
	}

	public static BookingType newInstance(final Course course)
	{
		return (BookingType) AbstractEntity.newInstance(new BookingType(course));
	}

}
