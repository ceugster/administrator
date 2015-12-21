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
@Table(name = "events_bank")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "bank_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "bank_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "bank_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "bank_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "bank_version")) })
public class Bank extends AbstractEntity
{
	/*
	 * id = bcNr + filialId
	 */
	@ManyToOne
	@JoinColumn(name = "bank_country_id", referencedColumnName = "country_id")
	private Country country;

	@ManyToOne(optional = true)
	@JoinColumn(name = "bank_zip_code_id", referencedColumnName = "zip_code_id")
	private ZipCode zipCode;

	/*
	 * Data
	 */
	@Id
	@Column(name = "bank_id")
	@GeneratedValue(generator = "events_bank_id_seq")
	@TableGenerator(name = "events_bank_id_seq", table = "events_sequence", initialValue = 1, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "bank_bc_nr")
	private String bcNr;

	@Basic
	@Column(name = "bank_filial_id")
	private String filialId;

	@Basic
	@Column(name = "bank_head_office")
	private String headOffice;

	@Basic
	@Column(name = "bank_bc_type")
	private String bcType;

	@Basic
	@Column(name = "bank_valid_from")
	private String validFrom;

	@Basic
	@Column(name = "bank_language")
	private String language;

	@Basic
	@Column(name = "bank_short_name")
	private String shortname;

	@Basic
	@Column(name = "bank_institute")
	private String institute;

	@Basic
	@Column(name = "bank_domicile")
	private String domicile;

	@Basic
	@Column(name = "bank_post_address")
	private String postAddress;

	@Basic
	@Column(name = "bank_zip")
	private String zip;

	@Basic
	@Column(name = "bank_city")
	private String city;

	@Basic
	@Column(name = "bank_phone")
	private String phone;

	@Basic
	@Column(name = "bank_fax")
	private String fax;

	@Basic
	@Column(name = "bank_post_account")
	private String postAccount;

	@Basic
	@Column(name = "bank_swift")
	private String swift;

	/*
	 * Constructor
	 */
	private Bank()
	{
		super();
	}

	public static Bank newInstance()
	{
		return (Bank) AbstractEntity.newInstance(new Bank());
	}

	@Override
	public void setId(Long id) 
	{
		this.id = id;
	}
	
	public Long getId()
	{
		return id;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Country getCountry() {
		return country;
	}

	public void setZipCode(ZipCode zipCode) {
		this.zipCode = zipCode;
		if (zipCode != null)
			this.zip = zipCode.getZip();
	}

	public ZipCode getZipCode() {
		return zipCode;
	}

	public void setBcNr(String bcNr) {
		this.bcNr = bcNr;
	}

	public String getBcNr() {
		return AbstractEntity.stringValueOf(bcNr);
	}

	public void setFilialId(String filialId) {
		this.filialId = filialId;
	}

	public String getFilialId() {
		return AbstractEntity.stringValueOf(filialId);
	}

	public void setHeadOffice(String headOffice) {
		this.headOffice = headOffice;
	}

	public String getHeadOffice() {
		return AbstractEntity.stringValueOf(headOffice);
	}

	public void setBcType(String bcType) {
		this.bcType = bcType;
	}

	public String getBcType() {
		return AbstractEntity.stringValueOf(bcType);
	}

	public void setValidFrom(String validFrom) {
		this.validFrom = validFrom;
	}

	public String getValidFrom() {
		return validFrom;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return AbstractEntity.stringValueOf(language);
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getShortname() {
		return AbstractEntity.stringValueOf(shortname);
	}

	public void setInstitute(String institute) {
		this.institute = institute;
	}

	public String getInstitute() {
		return AbstractEntity.stringValueOf(institute);
	}

	public void setDomicile(String domicile) {
		this.domicile = domicile;
	}

	public String getDomicile() {
		return AbstractEntity.stringValueOf(domicile);
	}

	public void setPostAddress(String postAddress) {
		this.postAddress = postAddress;
	}

	public String getPostAddress() {
		return AbstractEntity.stringValueOf(postAddress);
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getZip() {
		return AbstractEntity.stringValueOf(zip);
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return AbstractEntity.stringValueOf(city);
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhone() {
		return AbstractEntity.stringValueOf(phone);
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getFax() {
		return AbstractEntity.stringValueOf(fax);
	}

	public void setPostAccount(String postAccount) {
		this.postAccount = postAccount;
	}

	public String getPostAccount() {
		return AbstractEntity.stringValueOf(postAccount);
	}

	public void setSwift(String swift) {
		this.swift = swift;
	}

	public String getSwift() {
		return AbstractEntity.stringValueOf(swift);
	}
	
	public String toString()
	{
		return getInstitute() + " " + getDomicile() + " " + getZip() + " " + getCity() + " " + getSwift();
	}
}
