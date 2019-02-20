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

@Entity
@Table(name = "events_charity_person")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "charity_person_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "charity_person_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "charity_person_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "charity_person_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "charity_person_version")) })
public class CharityPerson extends AbstractEntity
{
	/**
	 * Data
	 */
	@Id
	@Column(name = "charity_person_id")
	@GeneratedValue(generator = "events_charity_person_id_seq")
	@TableGenerator(name = "events_charity_person_id_seq", table = "events_sequence")
	private Long id;

	/**
	 * References
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "charity_person_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;
	/*
	 * This field is reserved for id of external persons
	 */
	@Basic
	@Column(name = "charity_person_another_id")
	private String anotherId;

	/**
	 * Fields
	 */
	@Basic
	@Column(name = "charity_person_firstname")
	private String firstname;
	
	@Basic
	@Column(name = "charity_person_lastname")
	private String lastname;
	
	@Basic
	@Column(name = "charity_person_street")
	private String street;
	
	@Basic
	@Column(name = "charity_person_zip")
	private String zip;
	
	@Basic
	@Column(name = "charity_person_city")
	private String city;
	
	@Basic
	@Column(name = "charity_person_phone")
	private String phone;
	
	@Basic
	@Column(name = "charity_person_email")
	private String email;
	
	@Basic
	@Column(name = "charity_person_sex")
	private Sex sex;
	
	private CharityPerson()
	{
		super();
		this.sex = Sex.FEMALE;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	@Override
	public void setId(Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void copy(CharityPerson schoolClass)
	{
	}

	public void setFirstname(String firstname)
	{
		this.propertyChangeSupport.firePropertyChange("firstname", this.firstname, this.firstname = (firstname == null || firstname.isEmpty()) ? null : firstname);
	}

	public String getFirstname()
	{
		return stringValueOf(this.firstname);
	}
	
	public void setLastname(String lastname)
	{
		this.propertyChangeSupport.firePropertyChange("lastname", this.lastname, this.lastname = (lastname == null || lastname.isEmpty()) ? null : lastname);
	}

	public String getLastname()
	{
		return stringValueOf(this.lastname);
	}
	
	public void setStreet(String street)
	{
		this.propertyChangeSupport.firePropertyChange("street", this.street, this.street = (street == null || street.isEmpty()) ? null : street);
	}

	public String getStreet()
	{
		return stringValueOf(this.street);
	}
	
	public void setZip(String zip)
	{
		this.propertyChangeSupport.firePropertyChange("zip", this.zip, this.zip = (zip == null || zip.isEmpty()) ? null : zip);
	}

	public String getZip()
	{
		return stringValueOf(this.zip);
	}
	
	public void setCity(String city)
	{
		this.propertyChangeSupport.firePropertyChange("city", this.city, this.city = (city == null || city.isEmpty()) ? null : city);
	}

	public String getCity()
	{
		return stringValueOf(this.city);
	}
	
	public void setPhone(String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = (phone == null || phone.isEmpty()) ? null : phone);
	}

	public String getPhone()
	{
		return stringValueOf(this.phone);
	}
	
	public void setEmail(String email)
	{
		this.propertyChangeSupport.firePropertyChange("email", this.email, this.email = (email == null || email.isEmpty()) ? null : email);
	}

	public String getEmail()
	{
		return stringValueOf(this.email);
	}
	
	public void setSex(Sex sex)
	{
		this.propertyChangeSupport.firePropertyChange("sex", this.sex, this.sex = sex);
	}

	public Sex getSex()
	{
		return this.sex;
	}
	
	public static CharityPerson newInstance()
	{
		return (CharityPerson) AbstractEntity.newInstance(new CharityPerson());
	}
	
	public enum Sex
	{
		FEMALE, MALE;

		public String label()
		{
			switch (this)
			{
			case FEMALE:
			{
				return "weiblich";
			}
			case MALE:
			{
				return "männlich";
			}
			default:
			{
				throw new RuntimeException("Invalid sex");
			}
			}
		}

		public String salutation()
		{
			switch (this)
			{
			case FEMALE:
			{
				return "Frau";
			}
			case MALE:
			{
				return "Herrn";
			}
			default:
			{
				throw new RuntimeException("Invalid sex");
			}
			}
		}
	}
}
