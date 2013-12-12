package ch.eugster.events.persistence.model;

import java.util.Collection;
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

@Entity
@Table(name = "events_donation_purpose")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "donation_purpose_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "donation_purpose_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "donation_purpose_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "donation_purpose_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "donation_purpose_version")) })
public class DonationPurpose extends AbstractEntity
{

	@Id
	@Column(name = "donation_purpose_id")
	@GeneratedValue(generator = "events_donation_purpose_id_seq")
	@TableGenerator(name = "events_donation_purpose_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "donation_purpose_code")
	private String code;

	@Basic
	@Column(name = "donation_purpose_name")
	private String name;

	@Basic
	@Column(name = "donation_purpose_desc")
	private String description;

	/*
	 * Children
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "purpose")
	private final Collection<Donation> donations = new Vector<Donation>();

	private DonationPurpose()
	{
	}

	private DonationPurpose(String name)
	{
		this.name = name;
	}

	public void addDonation(final Donation donation)
	{
		donations.add(donation);
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	public Collection<Donation> getDonations()
	{
		return donations;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(this.name);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
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

	public static DonationPurpose newInstance()
	{
		return (DonationPurpose) AbstractEntity.newInstance(new DonationPurpose());
	}

	public static DonationPurpose newInstance(String name)
	{
		return (DonationPurpose) AbstractEntity.newInstance(new DonationPurpose(name));
	}

}
