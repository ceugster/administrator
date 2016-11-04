package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_address")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "address_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "address_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "address_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "address_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "address_version")) })
public class Address extends AbstractEntity implements Donator
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "address_id")
	@GeneratedValue(generator = "events_address_id_seq")
	@TableGenerator(name = "events_address_id_seq", table = "events_sequence", initialValue = 50000, allocationSize = 5)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "address_country_id", referencedColumnName = "country_id")
	private Country country;

	@ManyToOne(optional = true)
	@JoinColumn(name = "address_zip_code_id", referencedColumnName = "zip_code_id")
	private ZipCode zipCode;

	@Basic
	@Column(name = "address_name")
	private String name;

	@Basic
	@Column(name = "address_another_line")
	private String anotherLine;

	@Basic
	@Column(name = "address_address")
	private String address;

	@Basic
	@Column(name = "address_pob")
	private String pob;

	@Basic
	@Column(name = "address_zip")
	private String zip;

	@Basic
	@Column(name = "address_city")
	private String city;

	@Basic
	@Column(name = "address_phone")
	private String phone;

	@Basic
	@Column(name = "address_fax")
	private String fax;

	@Basic
	@Column(name = "address_email")
	private String email;

	@Basic
	@Column(name = "address_website")
	private String website;

	@Basic
	@Column(name = "address_province")
	private String province;

	@Basic
	@Column(name = "address_notes")
	private String notes;

	@OneToOne
	@JoinColumn(name = "address_address_salutation_id", referencedColumnName = "address_salutation_id")
	private AddressSalutation salutation;

	/*
	 * Members
	 */
	@OneToMany(cascade = ALL, mappedBy = "address")
	private List<Member> members = new Vector<Member>();

	/*
	 * Donations
	 */
	@OneToMany(cascade = ALL, mappedBy = "address")
	private final List<Donation> donations = new Vector<Donation>();

	/*
	 * PersonAddressLinks
	 */
	@OneToMany(cascade = ALL, mappedBy = "address")
	private List<LinkPersonAddress> links = new Vector<LinkPersonAddress>();

	/*
	 * AddressGroupDetail
	 */
	@OneToMany(cascade = ALL, mappedBy = "address")
	private List<AddressGroupMember> addressGroupMembers = new Vector<AddressGroupMember>();

	@OneToMany(cascade = ALL, mappedBy = "address")
	private List<BankAccount> bankAccounts = new Vector<BankAccount>();

	@OneToMany(cascade = ALL, mappedBy = "address")
	private List<AddressContact> contacts = new Vector<AddressContact>();

	/*
	 * Constructor
	 */
	private Address()
	{
		super();
	}

	public void addAddressGroupMember(final AddressGroupMember addressGroupMember)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers.add(addressGroupMember));
	}

	public void addDonation(final Donation donation)
	{
		this.propertyChangeSupport.firePropertyChange("donations", this.donations, this.donations.add(donation));
	}

	public void addLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("links", this.links, this.links.add(link));
	}
	
	public void addContact(AddressContact contact)
	{
		this.propertyChangeSupport.firePropertyChange("addContact", this.contacts, this.contacts.add(contact));
	}

	// public boolean isManualMailingAddress()
	// {
	// return this.manualMailingAddress;
	// }

	// public void setManualMailingAddress(boolean manualMailingAddress)
	// {
	// this.propertyChangeSupport.firePropertyChange("manualMailingAddress",
	// this.manualMailingAddress,
	// this.manualMailingAddress = manualMailingAddress);
	// }

	public void addMember(final Member member)
	{
		this.propertyChangeSupport.firePropertyChange("members", this.members, this.members.add(member));
	}

	public Address copy()
	{
		Address copy = Address.newInstance();
		copy.setAddress(this.getAddress());
		copy.setAnotherLine(this.getAnotherLine());
		copy.setCity(this.getCity());
		copy.setCountry(this.getCountry());
		copy.setEmail(this.getEmail());
		copy.setFax(this.getFax());
		copy.setName(this.getName());
		copy.setPhone(this.getPhone());
		copy.setPob(this.getPob());
		copy.setProvince(this.getProvince());
		copy.setSalutation(this.getSalutation());
		copy.setZip(this.getZip());
		copy.setWebsite(this.getWebsite());
		copy.setZipCode(this.getZipCode());
		return copy;
	}

	public String getAddress()
	{
		return AbstractEntity.stringValueOf(this.address).trim();
	}

	public List<AddressGroupMember> getAddressGroupMembers()
	{
		return this.addressGroupMembers;
	}

	public List<AddressGroupMember> getAddressAddressGroupMembers()
	{
		List<AddressGroupMember> addressGroupMembers = new ArrayList<AddressGroupMember>();
		AddressGroupMember[] members = this.addressGroupMembers.toArray(new AddressGroupMember[0]);
		for (AddressGroupMember member : members)
		{
			if (member.getLink() == null || member.getLink().isDeleted() || member.getLink().getPerson().isDeleted())
			{
				addressGroupMembers.add(member);
			}
		}
		return addressGroupMembers;
	}

	public List<AddressContact> getContacts()
	{
		return this.contacts;
	}
	
	public List<AddressContact> getValidContacts()
	{
		List<AddressContact> contacts = new ArrayList<AddressContact>();
		for (AddressContact contact : this.contacts)
		{
			if (contact.isValid())
			{
				contacts.add(contact);
			}
		}
		return contacts;
	}
	
	// public void setMailingAddress(String mailingAddress)
	// {
	// this.mailingAddress = mailingAddress;
	// }

	// public String getMailingAddress()
	// {
	// return stringValueOf(this.mailingAddress);
	// }

	public String getAnotherLine()
	{
		return AbstractEntity.stringValueOf(this.anotherLine);
	}

	public String getCity()
	{
		if (zipCode == null)
		{
			return AbstractEntity.stringValueOf(this.city).trim();
		}
		else
		{
			return zipCode.getCity();
		}
	}

	public Country getCountry()
	{
		return this.country;
	}

	public List<Donation> getDonations()
	{
		return donations;
	}

	public String getEmail()
	{
		return stringValueOf(email);
	}

	public String getFax()
	{
		return AbstractEntity.stringValueOf(this.fax);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public Member getMember(final Membership membership)
	{
		for (Member member : members)
		{
			if (!member.isDeleted() && member.getMembership().getId().equals(membership.getId()))
			{
				return member;
			}
		}
		return null;
	}

	public List<Member> getMembers()
	{
		return members;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(this.name);
	}

	public List<LinkPersonAddress> getPersonLinks()
	{
		return this.links;
	}

	public List<LinkPersonAddress> getValidLinks()
	{
		List<LinkPersonAddress> validLinks = new ArrayList<LinkPersonAddress>();
		for (LinkPersonAddress link : this.links)
		{
			if (!link.isDeleted() && !link.getPerson().isDeleted())
			{
				validLinks.add(link);
			}
		}
		return validLinks;
	}

	public String getPhone()
	{
		return AbstractEntity.stringValueOf(this.phone);
	}

	public String getPob()
	{
		return AbstractEntity.stringValueOf(this.pob);
	}

	public String getProvince()
	{
		if (this.getZipCode() != null)
		{
			return stringValueOf(this.getZipCode().getState());
		}
		return stringValueOf(this.province);
	}

	public AddressSalutation getSalutation()
	{
		return this.salutation;
	}

	public String getWebsite()
	{
		return AbstractEntity.stringValueOf(this.website);
	}

	public String getZip()
	{
		if (zipCode == null)
		{
			return AbstractEntity.stringValueOf(this.zip).trim();
		}
		else
		{
			return zipCode.getZip();
		}
	}

	public ZipCode getZipCode()
	{
		return zipCode;
	}

	public boolean hasDonationsForYear(final int year)
	{
		List<Donation> donations = this.getDonations();
		for (Donation donation : donations)
		{
			if (!donation.isDeleted() && donation.getDonationDate().get(Calendar.YEAR) == year)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isMember()
	{
		for (Member member : members)
		{
			if (!member.isDeleted())
			{
				return true;
			}
		}
		return false;
	}

	public void removeAddressGroupMember(final AddressGroupMember addressGroupMember)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers.remove(addressGroupMember));
	}

	public void removeLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("links", this.links, this.links.remove(link));
	}

	public void removeDonation(final Donation donation)
	{
		this.propertyChangeSupport.firePropertyChange("donations", this.donations, this.donations.remove(donation));
	}

	public void removeMember(final Member member)
	{
		this.propertyChangeSupport.firePropertyChange("members", this.members, this.members.remove(member));
	}

	public void setAddress(final String address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address.trim());
	}

	public void setAddressGroupMembers(final List<AddressGroupMember> addressGroupMembers)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers = addressGroupMembers);
	}

	public void setAnotherLine(final String anotherLine)
	{
		this.propertyChangeSupport.firePropertyChange("anotherLine", this.anotherLine, this.anotherLine = anotherLine);
	}

	public void setCity(final String city)
	{
		this.propertyChangeSupport.firePropertyChange("city", this.city, this.city = city == null ? null : city.trim());
	}

	public void setCountry(final Country country)
	{
		this.propertyChangeSupport.firePropertyChange("country", this.country, this.country = country);
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		for (AddressGroupMember member : addressGroupMembers)
		{
			member.setDeleted(deleted);
		}
		super.setDeleted(deleted);
	}

	public void undelete()
	{
		this.deleted = false;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public void setFax(final String fax)
	{
		this.propertyChangeSupport.firePropertyChange("fax", this.fax, this.fax = fax.trim());
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setMembers(final List<Member> members)
	{
		this.propertyChangeSupport.firePropertyChange("members", this.members, this.members = members);
	}

	public void setName(final String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public void setPersonLinks(final List<LinkPersonAddress> links)
	{
		this.propertyChangeSupport.firePropertyChange("links", this.links, this.links = links);
	}

	public void setPhone(final String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = phone);
	}

	public void setPob(final String pob)
	{
		this.propertyChangeSupport.firePropertyChange("pob", this.pob, this.pob = pob);
	}

	public void setProvince(final String province)
	{
		this.province = province;
	}

	public void setSalutation(final AddressSalutation salutation)
	{
		this.propertyChangeSupport.firePropertyChange("salutation", this.salutation, this.salutation = salutation);
	}

	public void setWebsite(final String website)
	{
		this.propertyChangeSupport.firePropertyChange("website", this.website, this.website = website.trim());
	}

	public void setZip(final String zip)
	{
		if (this.zip != zip)
		{
			this.propertyChangeSupport.firePropertyChange("zip", this.zip, this.zip = zip == null ? null : zip.trim());
		}
	}

	public void setZipCode(final ZipCode zipCode)
	{
		if (this.zipCode != zipCode)
		{
			this.propertyChangeSupport.firePropertyChange("zipCode", this.zipCode, this.zipCode = zipCode);
			setZip(zipCode == null ? null : zipCode.getZip());
			setCity(zipCode == null ? null : zipCode.getCity());
			setProvince(zipCode == null ? getProvince() : zipCode.getState());
		}
	}

	public void setNotes(String notes) 
	{
		this.propertyChangeSupport.firePropertyChange("notes", this.notes, this.notes = notes.trim());
	}

	public String getNotes() 
	{
		return stringValueOf(notes);
	}

	public void addBankAccount(final BankAccount bankAccount)
	{
		this.propertyChangeSupport.firePropertyChange("bankAccounts", this.bankAccounts,
				this.bankAccounts.add(bankAccount));
	}

	public List<BankAccount> getBankAccounts() {
		List<BankAccount> accounts = new ArrayList<BankAccount>();
		for (BankAccount account : this.bankAccounts)
		{
			if (!account.isDeleted())
			{
				accounts.add(account);
			}
		}
		return accounts;
	}

	public static Address newInstance()
	{
		return (Address) AbstractEntity.newInstance(new Address());
	}
}
