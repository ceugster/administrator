package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_bank")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "bank_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "bank_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "bank_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "bank_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "bank_version")) })
public class Bank extends AbstractEntity
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "bank_country_id", referencedColumnName = "country_id")
	private Country country;

	/*
	 * Data
	 */
	@Id
	@Column(name = "bank_id")
	@GeneratedValue(generator = "events_bank_id_seq")
	@TableGenerator(name = "events_bank_id_seq", table = "events_sequence")
	private Long id;

	/**
	 * bc_clearing + "|" + filial_id: unique key
	 */
	@Basic
	@Column(name = "bank_external_id")
	private String externalId;

	@Basic
	@Column(name = "bank_code")
	private String code;

	@Basic
	@Column(name = "bank_name")
	@Enumerated
	private String name;

	@Basic
	@Column(name = "bank_address")
	private String address;

	@Basic
	@Column(name = "bank_pob")
	private String pob;

	@Basic
	@Column(name = "bank_zip")
	private ZipCode zip;

	@Basic
	@Column(name = "bank_phone")
	private String phone;

	@Basic
	@Column(name = "bank_fax")
	private String fax;

	@Basic
	@Column(name = "bank_pc_account")
	private String pcAccount;

	@Basic
	@Column(name = "bank_swift")
	private String swift;

	@Basic
	@Column(name = "bank_bc_number")
	private String clearingNumber;

	/*
	 * Constructor
	 */
	private Bank()
	{
		super();
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

	public Country getCountry()
	{
		return country;
	}

	public void setCountry(Country country)
	{
		this.propertyChangeSupport.firePropertyChange("country", this.country, this.country = country);
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	public String getPob()
	{
		return pob;
	}

	public void setPob(String pob)
	{
		this.propertyChangeSupport.firePropertyChange("pob", this.pob, this.pob = pob);
	}

	public ZipCode getZip()
	{
		return zip;
	}

	public void setZip(ZipCode zip)
	{
		this.propertyChangeSupport.firePropertyChange("zip", this.zip, this.zip = zip);
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.propertyChangeSupport.firePropertyChange("phone", this.phone, this.phone = phone);
	}

	public String getFax()
	{
		return fax;
	}

	public void setFax(String fax)
	{
		this.propertyChangeSupport.firePropertyChange("fax", this.fax, this.fax = fax);
	}

	public String getPcAccount()
	{
		return pcAccount;
	}

	public void setPcAccount(String pcAccount)
	{
		this.propertyChangeSupport.firePropertyChange("pcAccount", this.pcAccount, this.pcAccount = pcAccount);
	}

	public String getSwift()
	{
		return swift;
	}

	public void setSwift(String swift)
	{
		this.propertyChangeSupport.firePropertyChange("swift", this.swift, this.swift = swift);
	}

	public String getClearingNumber()
	{
		return clearingNumber;
	}

	public void setClearingNumber(String clearingNumber)
	{
		this.propertyChangeSupport.firePropertyChange("clearingNumber", this.clearingNumber,
				this.clearingNumber = clearingNumber);
	}

	public void setExternalId(String externalId)
	{
		this.propertyChangeSupport.firePropertyChange("externalId", this.externalId, this.externalId = externalId);
	}

	public String getExternalId()
	{
		return externalId;
	}

}
