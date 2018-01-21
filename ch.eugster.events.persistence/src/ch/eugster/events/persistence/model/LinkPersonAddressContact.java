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
@DiscriminatorValue("L")
public class LinkPersonAddressContact extends Contact
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "contact_owner_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	private LinkPersonAddressContact()
	{
		super();
	}

	private LinkPersonAddressContact(final LinkPersonAddress link)
	{
		super();
		this.setLink(link);
		this.setCountry(link.getAddress().getCountry());
	}

	public LinkPersonAddress getLink()
	{
		return this.link;
	}

	public void setLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("link", this.link, this.link = link);
	}

	public static LinkPersonAddressContact newInstance(final LinkPersonAddress link)
	{
		return (LinkPersonAddressContact) AbstractEntity.newInstance(new LinkPersonAddressContact(link));
	}
}
