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
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_domain")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "domain_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "domain_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "domain_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "domain_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "domain_version")) })
public class Domain extends AbstractEntity
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "domain_id")
	@GeneratedValue(generator = "events_domain_id_seq")
	@TableGenerator(name = "events_domain_id_seq", table = "events_sequence", initialValue = 10, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "domain_code")
	private String code = "";

	@Basic
	@Column(name = "domain_name")
	private String name = "";

	@Basic
	@Column(name = "domain_desc")
	private String description = "";

	@Basic
	@Column(name = "domain_organization_name")
	private String organization = "";

	@Basic
	@Column(name = "domain_organization_address")
	private String address = "";

	@Basic
	@Column(name = "domain_organization_city")
	private String city = "";

	@Basic
	@Column(name = "domain_organization_phone")
	private String phone = "";

	@Basic
	@Column(name = "domain_organization_fax")
	private String fax = "";

	@Basic
	@Column(name = "domain_organization_email")
	private String email = "";

	@Basic
	@Column(name = "domain_organization_website")
	private String website = "";

	private Domain()
	{
		super();
	}

	public String getAddress()
	{
		return stringValueOf(address);
	}

	public String getCity()
	{
		return stringValueOf(city);
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	public String getEmail()
	{
		return stringValueOf(email);
	}

	public String getFax()
	{
		return stringValueOf(fax);
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

	public String getOrganization()
	{
		return stringValueOf(organization);
	}

	public String getPhone()
	{
		return stringValueOf(phone);
	}

	public String getWebsite()
	{
		return stringValueOf(website);
	}

	public void setAddress(final String address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	public void setCity(final String city)
	{
		this.propertyChangeSupport.firePropertyChange("city", this.city, this.city = city);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
	}

	public void setEmail(final String email)
	{
		this.propertyChangeSupport.firePropertyChange("email", this.email, this.email = email);
	}

	public void setFax(final String fax)
	{
		this.propertyChangeSupport.firePropertyChange("fax", this.fax, this.fax = fax);
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

	public void setOrganization(final String organization)
	{
		this.propertyChangeSupport.firePropertyChange("organization", this.organization,
				this.organization = organization);
	}

	public void setPhone(final String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = phone);
	}

	public void setWebsite(final String website)
	{
		this.propertyChangeSupport.firePropertyChange("website", this.website, this.website = website);
	}

	public static Domain newInstance()
	{
		return (Domain) AbstractEntity.newInstance(new Domain());
	}

}
