package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "events_contact")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "contact_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "contact_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "contact_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "contact_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "contact_version")) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("P")
public class PersonContact extends Contact
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "contact_owner_id", referencedColumnName = "person_id")
	private Person person;

	private PersonContact()
	{
		super();
	}

	private PersonContact(final Person person, Country country)
	{
		super();
		this.setPerson(person);
		this.setCountry(country);
	}

	public Person getPerson()
	{
		return this.person;
	}

	public void setPerson(final Person person)
	{
		this.propertyChangeSupport.firePropertyChange("person", this.person, this.person = person);
	}

	public static PersonContact newInstance(final Person person)
	{
		return (PersonContact) AbstractEntity.newInstance(new PersonContact(person, person.getCountry()));
	}
}
