package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
@DiscriminatorValue("A")
public class AddressContact extends Contact
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "contact_owner_id", referencedColumnName = "address_id")
	private Address address;

	private AddressContact()
	{
		super();
	}

	private AddressContact(Address address)
	{
		super();
		this.setAddress(address);
		this.setCountry(address.getCountry());
	}

	public Address getAddress()
	{
		return this.address;
	}

	public void setAddress(Address address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	@Override
	public Object clone()
	{
		AddressContact contact = AddressContact.newInstance(this.getAddress());
		copy(this);
		return contact;
	}

	public static AddressContact newInstance(Address address)
	{
		return (AddressContact) AbstractEntity.newInstance(new AddressContact(address));
	}

}
