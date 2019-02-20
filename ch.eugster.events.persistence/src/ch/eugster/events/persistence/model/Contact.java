package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_contact")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "contact_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "contact_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "contact_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "contact_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "contact_version")) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "contact_discriminator", discriminatorType = DiscriminatorType.STRING)
public class Contact extends AbstractEntity
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "contact_contact_type_id", referencedColumnName = "contact_type_id")
	private ContactType contactType;

	@ManyToOne
	@JoinColumn(name = "contact_country_id", referencedColumnName = "country_id")
	private Country country;

	/*
	 * Data
	 */
	@Id
	@Column(name = "contact_id")
	@GeneratedValue(generator = "events_contact_id_seq")
	@TableGenerator(name = "events_contact_id_seq", table = "events_sequence", initialValue = 5000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "contact_name", columnDefinition = "VARCHAR(255)")
	private String name;

	@Basic
	@Column(name = "contact_value")
	@Lob
	private String value;

	protected Contact()
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

	public String getValue()
	{
		return stringValueOf(this.value);
	}

	public void setValue(String value)
	{
		this.propertyChangeSupport.firePropertyChange("value", this.value, this.value = value);
	}

	public ContactType getType()
	{
		return this.contactType;
	}

	public void setType(ContactType type)
	{
		this.propertyChangeSupport.firePropertyChange("contactType", this.contactType, this.contactType = type);
	}

	public void copy(Contact source)
	{
		this.setName(source.getName());
		this.setValue(source.getValue());
		this.setType(source.getType());
		this.setCountry(source.getCountry());
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return stringValueOf(name);
	}

	public void setCountry(Country country)
	{
		this.country = country;
	}

	public Country getCountry()
	{
		return country;
	}
}
