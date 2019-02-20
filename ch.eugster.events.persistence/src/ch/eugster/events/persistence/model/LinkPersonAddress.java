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
import javax.persistence.CascadeType;
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
@Table(name = "events_pa_link")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "pa_link_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "pa_link_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "pa_link_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "pa_link_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "pa_link_version")) })
public class LinkPersonAddress extends AbstractEntity implements Donator, AddressGroupMemberCapable
{
	/**
	 * References
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "pa_link_person_id", referencedColumnName = "person_id")
	private Person person;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "pa_link_address_id", referencedColumnName = "address_id")
	private Address address;

	@ManyToOne()
	@JoinColumn(name = "pa_link_address_type_id", referencedColumnName = "address_type_id")
	private AddressType addressType;

	/**
	 * Data
	 */
	@Id
	@Column(name = "pa_link_id")
	@GeneratedValue(generator = "events_pa_link_id_seq")
	@TableGenerator(name = "events_pa_link_id_seq", table = "events_sequence", initialValue = 50000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "pa_link_function")
	private String function;

	@Basic
	@Column(name = "pa_link_email")
	private String email;

	@Basic
	@Column(name = "pa_link_phone")
	private String phone;

	/*
	 * Contacts
	 */
	@OneToMany(cascade = ALL, mappedBy = "link")
	private final List<LinkPersonAddressContact> contacts = new Vector<LinkPersonAddressContact>();

	@OneToMany(cascade = ALL, mappedBy = "link")
	private List<Member> members = new Vector<Member>();

	@OneToMany(cascade = ALL, mappedBy = "link")
	private List<Donation> donations = new Vector<Donation>();

	@OneToMany(cascade = ALL, mappedBy = "link")
	private List<AddressGroupMember> addressGroupMembers = new Vector<AddressGroupMember>();

	@OneToMany(cascade = ALL, mappedBy = "link")
	private List<Participant> participants = new Vector<Participant>();

	@OneToMany(cascade = ALL, mappedBy = "link")
	private final List<LinkPersonAddressExtendedField> extendedFields = new Vector<LinkPersonAddressExtendedField>();

	@OneToOne(cascade = ALL, optional = true)
	@JoinColumn(name = "pa_link_guide_id", referencedColumnName = "guide_id")
	private Guide guide;

	@OneToOne(optional = true, cascade = ALL, mappedBy = "link")
	private Visitor visitor;

	@OneToOne(optional = true, cascade = ALL, mappedBy = "link")
	private Teacher teacher;


	private LinkPersonAddress()
	{
		super();
	}

	private LinkPersonAddress(final Address address)
	{
		super();
		this.setAddress(address);
		address.addLink(this);
	}

	private LinkPersonAddress(final Person person)
	{
		super();
		this.setPerson(person);
		person.addLink(this);
	}

	private LinkPersonAddress(final Person person, final Address address)
	{
		super();
		this.setPerson(person);
		person.addLink(this);
		this.setAddress(address);
		address.addLink(this);
	}

	@Override
	public void addAddressGroupMember(final AddressGroupMember member)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers.add(member));
	}

	public void addContact(final LinkPersonAddressContact contact)
	{
		this.propertyChangeSupport.firePropertyChange("addContact", this.contacts, this.contacts.add(contact));
	}

	public void removeContact(final LinkPersonAddressContact contact)
	{
		this.propertyChangeSupport.firePropertyChange("removeContact", this.contacts, this.contacts.remove(contact));
	}

	@Override
	public void addDonation(final Donation donation)
	{
		this.propertyChangeSupport.firePropertyChange("donations", this.donations, this.donations.add(donation));
	}

	public void addExtendedFields(final LinkPersonAddressExtendedField extendedField)
	{
		this.propertyChangeSupport.firePropertyChange("addField", this.extendedFields,
				this.extendedFields.add(extendedField));
	}

	public void addMember(final Member member)
	{
		this.propertyChangeSupport.firePropertyChange("members", this.members, this.members.add(member));
	}

	public void addParticipant(final Participant participant)
	{
		this.propertyChangeSupport.firePropertyChange("participants", this.participants,
				this.participants.add(participant));
	}

	public Address getAddress()
	{
		return this.address;
	}

	@Override
	public List<AddressGroupMember> getAddressGroupMembers()
	{
		final List<AddressGroupMember> members = new ArrayList<AddressGroupMember>();
		for (final AddressGroupMember member : this.addressGroupMembers)
		{
			if (!member.isDeleted() && !member.getAddressGroup().isDeleted())
			{
				members.add(member);
			}
		}
		return members;
	}

	public AddressType getAddressType()
	{
		return this.addressType;
	}
	
	public boolean isInAddressGroup(final AddressGroup addressGroup)
	{
		final List<AddressGroupMember> addressGroupMembers = this.getAddressGroupMembers();
		for (final AddressGroupMember addressGroupMember : addressGroupMembers)
		{
			if (!addressGroupMember.isDeleted() && addressGroupMember.getAddressGroup().getId().equals(addressGroup.getId()))
			{
				return true;
			}
		}
		return false;
	}

	public List<LinkPersonAddressContact> getContacts()
	{
		return this.contacts;
	}

	public List<LinkPersonAddressContact> getValidContacts()
	{
		final List<LinkPersonAddressContact> contacts = new ArrayList<LinkPersonAddressContact>();
		for (final LinkPersonAddressContact contact : this.contacts)
		{
			if (contact.isValid())
			{
				contacts.add(contact);
			}
		}
		return contacts;
	}

	@Override
	public List<Donation> getDonations()
	{
		return this.donations;
	}

	public List<Donation> getValidDonations()
	{
		final List<Donation> donations = new ArrayList<Donation>();
		for (final Donation donation : this.donations)
		{
			if (!donation.isDeleted())
			{
				donations.add(donation);
			}
		}
		return donations;
	}
	
	@Override
	public boolean isValid()
	{
		return !this.isDeleted() && !this.getPerson().isDeleted();
	}
	
	public String getEmail()
	{
		return AbstractEntity.stringValueOf(this.email);
	}

	public List<LinkPersonAddressExtendedField> getExtendedFields()
	{
		return this.extendedFields;
	}

	public String getFunction()
	{
		return AbstractEntity.stringValueOf(this.function);
	}

	public Guide getGuide()
	{
		return this.guide;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public Member getMember(final Membership membership)
	{
		for (final Member member : this.members)
		{
			if (!member.isDeleted())
			{
				if (member.getMembership().getId().equals(membership.getId()))
				{
					return member;
				}
			}
		}
		return null;
	}
	
	public boolean hasValidMembers()
	{
		for (final Member member : this.members)
		{
			if (!member.isDeleted())
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasValidDonations()
	{
		for (final Donation donation : this.donations)
		{
			if (!donation.isDeleted())
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasValidAddressGroupMembers()
	{
		for (final AddressGroupMember member : this.addressGroupMembers)
		{
			if (!member.isDeleted())
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasValidParticipants()
	{
		for (final Participant participant : this.participants)
		{
			if (!participant.isDeleted())
			{
				return true;
			}
		}
		return false;
	}

	public List<Member> getMembers()
	{
		return this.members;
	}

	public List<Member> getValidMembers()
	{
		final List<Member> members = new ArrayList<Member>();
		for (final Member member : this.members)
		{
			if (!member.isDeleted())
			{
				members.add(member);
			}
		}
		return members;
	}

	public List<Participant> getParticipants()
	{
		return this.participants;
	}

	public List<Participant> getValidParticipants()
	{
		final List<Participant> participants = new ArrayList<Participant>();
		for (final Participant participant : this.participants)
		{
			if (!participant.isDeleted())
			{
				participants.add(participant);
			}
		}
		return participants;
	}

	/**
	 * 
	 * @return amount
	 */
	public Person getPerson()
	{
		return this.person;
	}

	public String getPhone()
	{
		return AbstractEntity.stringValueOf(this.phone);
	}

	public Teacher getTeacher()
	{
		return this.teacher;
	}

	public Visitor getVisitor()
	{
		return this.visitor;
	}

	public boolean hasDonationsForYear(final int year)
	{
		final List<Donation> donations = this.getDonations();
		for (final Donation donation : donations)
		{
			if (!donation.isDeleted() && donation.getDonationDate().get(Calendar.YEAR) == year)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void removeAddressGroupMember(final AddressGroupMember member)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers.remove(member));
	}

	public void removeContact(final LinkPersonAddressExtendedField contact)
	{
		this.propertyChangeSupport.firePropertyChange("removeContact", this.contacts, this.contacts.remove(contact));
	}

	@Override
	public void removeDonation(final Donation donation)
	{
		this.propertyChangeSupport.firePropertyChange("donations", this.donations, this.donations.remove(donation));
	}

	public void removeExtendedFields(final LinkPersonAddressExtendedField extendedField)
	{
		this.propertyChangeSupport.firePropertyChange("removeField", this.extendedFields,
				this.extendedFields.remove(extendedField));
	}

	public void removeMember(final Member member)
	{
		this.propertyChangeSupport.firePropertyChange("members", this.members, this.members.remove(member));
	}

	public void removeParticipant(final Participant participant)
	{
		this.propertyChangeSupport.firePropertyChange("participants", this.participants,
				this.participants.remove(participant));
	}

	public void setAddress(final Address address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
		for (final Member member : this.members)
		{
			if (!member.getAddress().getId().equals(address.getId()))
			{
				member.setAddress(address);
			}
		}
		for (final Donation donation : this.donations)
		{
			if (!donation.getAddress().getId().equals(address.getId()))
			{
				donation.setAddress(address);
			}
		}
	}

	public void setAddressGroupMembers(final List<AddressGroupMember> addressGroupMembers)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers = addressGroupMembers);
	}

	public void setAddressType(final AddressType addressType)
	{
		this.propertyChangeSupport.firePropertyChange("addressType", this.addressType, this.addressType = addressType);
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		for (final AddressGroupMember member : this.addressGroupMembers)
		{
			member.setDeleted(deleted);
		}
		for (final Member member : this.members)
		{
			member.setDeleted(deleted);
		}
		if (this.getAddress().getValidLinks().size() == 1 && this.getAddress().getValidLinks().get(0).getId().equals(this.getId()))
		{
			this.getAddress().setDeleted(deleted);
		}
		if (this.guide != null)
		{
			this.guide.setDeleted(deleted);
		}	
		for (final Donation donation : this.donations)
		{
			donation.setDeleted(deleted);
		}
		for (final Participant participant : this.participants)
		{
			participant.setDeleted(deleted);
		}
		super.setDeleted(deleted);
	}

	public void setDonations(final List<Donation> donations)
	{
		this.propertyChangeSupport.firePropertyChange("donations", this.donations, this.donations = donations);
	}

	public void setEmail(final String email)
	{
		this.propertyChangeSupport.firePropertyChange("email", this.email, this.email = email);
	}

	public void setFunction(final String function)
	{
		this.propertyChangeSupport.firePropertyChange("function", this.function, this.function = function);
	}

	public void setGuide(final Guide guide)
	{
		this.propertyChangeSupport.firePropertyChange("guide", this.guide, this.guide = guide);
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

	public void setParticipants(final List<Participant> participants)
	{
		this.propertyChangeSupport.firePropertyChange("participants", this.participants,
				this.participants = participants);
	}

	public void setPerson(final Person person)
	{
		this.propertyChangeSupport.firePropertyChange("person", this.person, this.person = person);
	}

	public void setPhone(final String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = phone);
	}

	public void setTeacher(final Teacher teacher)
	{
		this.propertyChangeSupport.firePropertyChange("teacher", this.teacher, this.teacher = teacher);
	}

	public void setVisitor(final Visitor visitor)
	{
		this.propertyChangeSupport.firePropertyChange("visitor", this.visitor, this.visitor = visitor);
	}

	public static LinkPersonAddress newInstance()
	{
		return (LinkPersonAddress) AbstractEntity.newInstance(new LinkPersonAddress());
	}

	public static LinkPersonAddress newInstance(final Address address)
	{
		return (LinkPersonAddress) AbstractEntity.newInstance(new LinkPersonAddress(address));
	}

	public static LinkPersonAddress newInstance(final Person person)
	{
		return (LinkPersonAddress) AbstractEntity.newInstance(new LinkPersonAddress(person));
	}

	public static LinkPersonAddress newInstance(final Person person, final Address address)
	{
		return (LinkPersonAddress) AbstractEntity.newInstance(new LinkPersonAddress(person, address));
	}
}
