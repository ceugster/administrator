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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

@Entity
@Table(name = "events_booking_type_proposition")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "booking_type_proposition_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "booking_type_proposition_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "booking_type_proposition_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "booking_type_proposition_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "booking_type_proposition_version")) })
public class BookingTypeProposition extends AbstractEntity
{

	@Transient
	private static NumberFormat nf;

	@OneToOne(optional = true)
	@JoinColumn(name = "booking_type_proposition_membership_id", referencedColumnName = "membership_id")
	private Membership membership;

	/**
	 * Data
	 */
	@Id
	@Column(name = "booking_type_proposition_id")
	@GeneratedValue(generator = "events_booking_type_proposition_id_seq")
	@TableGenerator(name = "events_booking_type_proposition_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "booking_type_proposition_code")
	private String code;

	@Basic
	@Column(name = "booking_type_proposition_name")
	private String name;

	@Basic
	@Column(name = "booking_type_proposition_price")
	private double price;

	@Basic
	@Column(name = "booking_type_proposition_max_age")
	private int maxAge;

	@Basic
	@Column(name = "booking_type_proposition_annulation_charges")
	private double annulationCharges;

	private BookingTypeProposition()
	{
		super();
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

	public static BookingTypeProposition newInstance()
	{
		return (BookingTypeProposition) AbstractEntity.newInstance(new BookingTypeProposition());
	}
}
