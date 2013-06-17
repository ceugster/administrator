package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_person")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "person_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "person_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "person_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "person_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "person_version")) })
public class Person extends AbstractEntity
{
	/*
	 * References
	 */
	@ManyToOne(optional = true, cascade = { PERSIST, MERGE, REFRESH })
	@JoinColumn(name = "person_domain_id", referencedColumnName = "domain_id")
	private Domain domain;

	@ManyToOne(optional = true)
	@JoinColumn(name = "person_country_id", referencedColumnName = "country_id")
	private Country country;

	@ManyToOne(optional = true)
	@JoinColumn(name = "person_person_title_id", referencedColumnName = "person_title_id")
	private PersonTitle title;

	/*
	 * Data
	 */
	@Id
	@Column(name = "person_id")
	@GeneratedValue(generator = "events_person_id_seq")
	@TableGenerator(name = "events_person_id_seq", table = "events_sequence", initialValue = 50000)
	private Long id;

	@Basic
	@Column(name = "person_birthdate")
	private Long birthdate;

	@Basic
	@Column(name = "person_form")
	@Enumerated
	private PersonForm form;

	@Basic
	@Column(name = "person_firstname")
	private String firstname;

	@Basic
	@Column(name = "person_lastname")
	private String lastname;

	@Basic
	@Column(name = "person_another_line")
	private String anotherLine;

	@Basic
	@Column(name = "person_profession")
	private String profession;

	@Basic
	@Column(name = "person_notice")
	private String notes;

	@Basic
	@Column(name = "person_phone")
	private String phone;

	@Basic
	@Column(name = "person_email")
	private String email;

	@Basic
	@Column(name = "person_website")
	private String website;

	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "person_default_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress defaultLink;

	@OneToOne
	@JoinColumn(name = "person_person_sex_id", referencedColumnName = "person_sex_id")
	private PersonSex sex;

	@OneToMany(cascade = ALL, mappedBy = "person")
	private Collection<PersonExtendedField> extendedFields = new Vector<PersonExtendedField>();

	/*
	 * AddressLinks
	 */
	@OneToMany(cascade = ALL, mappedBy = "person")
	private Collection<LinkPersonAddress> links = new Vector<LinkPersonAddress>();

	/*
	 * Constructor
	 */
	private Person()
	{
		super();
	}

	private Person(final Domain domain)
	{
		super();
		this.setDomain(domain);
	}

	public void addExtendedFields(final PersonExtendedField extendedField)
	{
		this.propertyChangeSupport.firePropertyChange("addField", this.extendedFields,
				this.extendedFields.add(extendedField));
	}

	public void addLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("links", this.links, this.links.add(link));
		if (defaultLink == null)
		{
			defaultLink = link;
		}
	}

	public Person copy()
	{
		Person copy = Person.newInstance();
		copy.setAnotherLine(this.getAnotherLine());
		copy.setBirthdate(this.getBirthdate());
		copy.setCountry(this.getCountry());
		copy.setDeleted(this.isDeleted());
		copy.setDomain(this.getDomain());
		copy.setEmail(this.getEmail());
		copy.setFirstname(this.getFirstname());
		copy.setForm(this.getForm());
		copy.setLastname(this.getLastname());
		copy.setNotes(this.getNotes());
		copy.setPhone(this.getPhone());
		copy.setProfession(this.getProfession());
		copy.setSex(this.getSex());
		copy.setTitle(this.getTitle());
		copy.setWebsite(this.getWebsite());
		return copy;
	}

	public Collection<LinkPersonAddress> getActiveLinks()
	{
		Collection<LinkPersonAddress> links = new ArrayList<LinkPersonAddress>();
		for (LinkPersonAddress link : this.getLinks())
		{
			if (!link.isDeleted())
			{
				links.add(link);
			}
		}
		return links;
	}

	public Collection<AddressGroupMember> getAddressGroupMembers()
	{
		Collection<AddressGroupMember> members = new ArrayList<AddressGroupMember>();
		Collection<LinkPersonAddress> links = this.getActiveLinks();
		for (LinkPersonAddress link : links)
		{
			if (!link.isDeleted())
			{
				members.addAll(link.getAddressGroupMembers());
			}
		}
		return members;
	}

	public String getAnotherLine()
	{
		return AbstractEntity.stringValueOf(this.anotherLine);
	}

	public Long getBirthdate()
	{
		return this.birthdate;
	}

	public Date getBirthday()
	{
		Long birthdate = getBirthdate();
		if (birthdate == null)
		{
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		long birth = birthdate.longValue();
		int year = calendar.get(Calendar.YEAR);
		if (birth < 1900 || year < birth)
		{
			calendar.setTimeInMillis(birth);
			return calendar.getTime();
		}
		return null;
	}

	public Integer getBirthyear()
	{
		if (birthdate == null)
		{
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		long birth = birthdate.longValue();
		int year = GregorianCalendar.getInstance().get(Calendar.YEAR);
		if (birth < 1900 || year < birth)
		{
			calendar.setTimeInMillis(birth);
		}
		else
		{
			calendar.set(Calendar.DATE, 1);
			calendar.set(Calendar.MONTH, 0);
			calendar.set(Calendar.YEAR, (int) birth);
		}
		return Integer.valueOf(calendar.get(Calendar.YEAR));
	}

	public Country getCountry()
	{
		return country;
	}

	public LinkPersonAddress getDefaultLink()
	{
		return defaultLink;
	}

	public Domain getDomain()
	{
		return this.domain;
	}

	public Collection<Donation> getDonations()
	{
		Collection<Donation> donations = new ArrayList<Donation>();
		Collection<LinkPersonAddress> links = this.getActiveLinks();
		for (LinkPersonAddress link : links)
		{
			donations.addAll(link.getDonations());
		}
		return donations;
	}

	public String getEmail()
	{
		return AbstractEntity.stringValueOf(this.email);
	}

	public Collection<PersonExtendedField> getExtendedFields()
	{
		return extendedFields;
	}

	public String getFirstname()
	{
		return AbstractEntity.stringValueOf(this.firstname);
	}

	public PersonForm getForm()
	{
		if (this.form == null)
			this.form = PersonForm.POLITE;
		return this.form;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getLastname()
	{
		return AbstractEntity.stringValueOf(this.lastname);
	}

	public Collection<LinkPersonAddress> getLinks()
	{
		return this.links;
	}

	public Member getMember(final Membership membership)
	{
		Member member = null;
		for (LinkPersonAddress link : links)
		{
			member = link.getMember(membership);
			if (member != null)
			{
				return member;
			}
		}
		return null;
	}

	public Collection<Member> getMembers()
	{
		Collection<Member> members = new Vector<Member>();
		for (LinkPersonAddress link : links)
		{
			members.addAll(link.getMembers());
		}
		return members;
	}

	public String getNotes()
	{
		return stringValueOf(notes);
	}

	public Collection<Participant> getParticipants()
	{
		Collection<Participant> participants = new ArrayList<Participant>();
		Collection<LinkPersonAddress> links = this.getActiveLinks();
		for (LinkPersonAddress link : links)
		{
			if (!link.isDeleted())
			{
				participants.addAll(link.getParticipants());
			}
		}
		return participants;
	}

	public String getPhone()
	{
		return AbstractEntity.stringValueOf(this.phone);
	}

	public String getProfession()
	{
		return AbstractEntity.stringValueOf(this.profession);
	}

	public PersonSex getSex()
	{
		return this.sex;
	}

	public PersonTitle getTitle()
	{
		return this.title;
	}

	public String getWebsite()
	{
		return AbstractEntity.stringValueOf(this.website);
	}

	public boolean hasBirthdate()
	{
		return this.getBirthday() != null;
	}

	public boolean hasBirthyear()
	{
		return this.getBirthyear() != null;
	}

	public boolean hasDonationsForYear(final int year)
	{
		Collection<Donation> donations = this.getDonations();
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
		for (LinkPersonAddress link : links)
		{
			for (Member member : link.getMembers())
			{
				if (!member.isDeleted())
				{
					return true;
				}
			}
		}
		return false;
	}

	public void removeExtendedFields(final PersonExtendedField extendedField)
	{
		this.propertyChangeSupport.firePropertyChange("removeField", this.extendedFields,
				this.extendedFields.remove(extendedField));
	}

	public void removeLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("links", this.links, this.links.remove(link));
	}

	public void setAnotherLine(final String anotherLine)
	{
		this.propertyChangeSupport.firePropertyChange("anotherLine", this.anotherLine, this.anotherLine = anotherLine);
	}

	public void setBirthdate(final Long birthdate)
	{
		this.propertyChangeSupport.firePropertyChange("birthdate", this.birthdate, this.birthdate = birthdate);
	}

	public void setCountry(final Country country)
	{
		this.country = country;
	}

	public void setDefaultLink(final LinkPersonAddress defaultLink)
	{
		this.defaultLink = defaultLink;
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		Collection<LinkPersonAddress> links = this.getActiveLinks();
		if (links != null)
		{
			for (LinkPersonAddress link : links)
			{
				link.setDeleted(deleted);
			}
		}
		super.setDeleted(deleted);
	}

	public void setDomain(final Domain domain)
	{
		this.propertyChangeSupport.firePropertyChange("domain", this.domain, this.domain = domain);
	}

	public void setEmail(final String email)
	{
		this.propertyChangeSupport.firePropertyChange("email", this.email, this.email = email);
	}

	public void setFirstname(final String firstname)
	{
		this.propertyChangeSupport.firePropertyChange("firstname", this.firstname, this.firstname = firstname);
	}

	public void setForm(final PersonForm form)
	{
		this.propertyChangeSupport.firePropertyChange("form", this.form, this.form = form);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLastname(final String lastname)
	{
		this.propertyChangeSupport.firePropertyChange("lastname", this.lastname, this.lastname = lastname);
	}

	public void setLinks(final Collection<LinkPersonAddress> links)
	{
		this.propertyChangeSupport.firePropertyChange("links", this.links, this.links = links);
	}

	public void setNotes(final String notes)
	{
		this.notes = notes;
	}

	public void setPhone(final String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = phone);
	}

	public void setProfession(final String profession)
	{
		this.propertyChangeSupport.firePropertyChange("profession", this.profession, this.profession = profession);
	}

	public void setSex(final PersonSex sex)
	{
		this.propertyChangeSupport.firePropertyChange("sex", this.sex, this.sex = sex);
	}

	public void setTitle(final PersonTitle title)
	{
		this.propertyChangeSupport.firePropertyChange("title", this.title, this.title = title);
	}

	public void setWebsite(final String website)
	{
		this.propertyChangeSupport.firePropertyChange("website", this.website, this.website = website);
	}

	public static Person newInstance()
	{
		return (Person) AbstractEntity.newInstance(new Person());
	}

	public static Person newInstance(final Domain domain)
	{
		return (Person) AbstractEntity.newInstance(new Person(domain));
	}
}
