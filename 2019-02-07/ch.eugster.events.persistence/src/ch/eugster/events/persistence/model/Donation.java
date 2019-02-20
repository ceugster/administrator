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

@Entity
@Table(name = "events_donation")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "donation_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "donation_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "donation_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "donation_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "donation_version")) })
public class Donation extends AbstractEntity
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "donation_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	@ManyToOne
	@JoinColumn(name = "donation_address_id", referencedColumnName = "address_id")
	private Address address;

	@ManyToOne
	@JoinColumn(name = "donation_donation_purpose_id", referencedColumnName = "donation_purpose_id")
	private DonationPurpose purpose;

	@ManyToOne
	@JoinColumn(name = "donation_domain_id", referencedColumnName = "domain_id")
	private Domain domain;

	/*
	 * Data
	 */
	@Id
	@Column(name = "donation_id")
	@GeneratedValue(generator = "events_donation_id_seq")
	@TableGenerator(name = "events_donation_id_seq", table = "events_sequence", initialValue = 5000, allocationSize = 5)
	private Long id;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "donation_date")
	private Calendar date;

	@Basic
	@Column(name = "donation_amount")
	private double amount;

	@Basic
	@Column(name = "donation_year")
	private int year;

	private Donation()
	{
		super();
	}

	private Donation(final Address address)
	{
		super();
		this.setAddress(address);
	}

	private Donation(final LinkPersonAddress link)
	{
		super();
		this.setLink(link);
	}

	public boolean isValid()
	{
		return !this.deleted && ((this.getLink() == null) ? this.getAddress().isValid() : this.getLink().isValid());
	}
	
	public Address getAddress()
	{
		return this.address;
	}

	public double getAmount()
	{
		return this.amount;
	}

	public Domain getDomain()
	{
		return domain;
	}

	public Calendar getDonationDate()
	{
		return this.date;
	}

	public int getDonationYear()
	{
		return date.get(Calendar.YEAR);
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

	public DonationPurpose getPurpose()
	{
		return this.purpose;
	}

	public int getYear()
	{
		return this.year;
	}

	public void setAddress(final Address address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	public void setAmount(final double amount)
	{
		this.propertyChangeSupport.firePropertyChange("amount", this.amount, this.amount = amount);
	}

	public void setDomain(final Domain domain)
	{
		this.domain = domain;
	}

	public void setDonationDate(final Calendar date)
	{
		this.propertyChangeSupport.firePropertyChange("date", this.date, this.date = date);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("link", this.link, this.link = link);
		this.setAddress(link.getAddress());
	}

	public void setPurpose(final DonationPurpose purpose)
	{
		this.propertyChangeSupport.firePropertyChange("purpose", this.purpose, this.purpose = purpose);
	}

	public void setYear(final int year)
	{
		this.propertyChangeSupport.firePropertyChange("year", this.year, this.year = year);
	}

	public static Donation newInstance()
	{
		return (Donation) AbstractEntity.newInstance(new Donation());
	}

	public static Donation newInstance(final Address address)
	{
		return (Donation) AbstractEntity.newInstance(new Donation(address));
	}

	public static Donation newInstance(final LinkPersonAddress person)
	{
		return (Donation) AbstractEntity.newInstance(new Donation(person));
	}

}
