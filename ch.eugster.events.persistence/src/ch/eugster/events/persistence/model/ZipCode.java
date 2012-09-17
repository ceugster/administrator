package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_zip_code")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "zip_code_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "zip_code_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "zip_code_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "zip_code_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "zip_code_version")) })
@Customizer(DeletedFilter.class)
public class ZipCode extends AbstractEntity
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "zip_code_country_id", referencedColumnName = "country_id")
	private Country country;

	/**
	 * 
	 */
	@Id
	@Column(name = "zip_code_id")
	private Long id;

	@Basic
	@Column(name = "zip_code_type")
	private int type;

	@Basic
	@Column(name = "zip_code_zip")
	private String zip;

	@Basic
	@Column(name = "zip_code_postfix")
	private int postfix;

	@Basic
	@Column(name = "zip_code_city_long")
	private String city;

	@Basic
	@Column(name = "zip_code_state")
	private String state;

	private ZipCode()
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

	public int getType()
	{
		return this.type;
	}

	public void setType(int type)
	{
		this.propertyChangeSupport.firePropertyChange("type", this.type, this.type = type);
	}

	public String getZip()
	{
		return this.zip;
	}

	public void setZip(String zip)
	{
		this.propertyChangeSupport.firePropertyChange("zip", this.zip, this.zip = zip);
		this.zip = zip;
	}

	public int getPostfix()
	{
		return this.postfix;
	}

	public void setPostfix(int postfix)
	{
		this.propertyChangeSupport.firePropertyChange("postfix", this.postfix, this.postfix = postfix);
	}

	public String getCity()
	{
		return this.city;
	}

	public void setCity(String city)
	{
		this.propertyChangeSupport.firePropertyChange("city", this.city, this.city = city);
		this.city = city;
	}

	public String getState()
	{
		return this.state;
	}

	public void setState(String state)
	{
		this.propertyChangeSupport.firePropertyChange("state", this.state, this.state = state);
		this.state = state;
	}

	public void setCountry(Country country)
	{
		this.country = country;
	}

	public Country getCountry()
	{
		return country;
	}

	@Override
	public Object clone()
	{
		ZipCode zipCode = ZipCode.newInstance();
		zipCode.setCity(this.getCity());
		zipCode.setState(this.getState());
		zipCode.setPostfix(this.getPostfix());
		zipCode.setType(this.getType());
		zipCode.setZip(this.getZip());
		return zipCode;
	}

	public static ZipCode newInstance()
	{
		return (ZipCode) AbstractEntity.newInstance(new ZipCode());
	}

}
