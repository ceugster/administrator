package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_address_group_member")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "address_group_member_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "address_group_member_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "address_group_member_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "address_group_member_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "address_group_member_version")) })
public class AddressGroupMember extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "address_group_member_address_group_id", referencedColumnName = "address_group_id")
	private AddressGroup addressGroup = null;

	@ManyToOne(optional = false)
	@JoinColumn(name = "address_group_member_address_id", referencedColumnName = "address_id")
	private Address address;

	@ManyToOne
	@JoinColumn(name = "address_group_member_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link = null;

	@ManyToOne(optional = false)
	@JoinColumn(name = "address_group_member_copied_from", referencedColumnName = "address_group_id")
	private AddressGroup copiedFrom = null;

	/**
	 * Data
	 */
	@Id
	@Column(name = "address_group_member_id")
	@GeneratedValue(generator = "events_address_group_member_id_seq")
	@TableGenerator(name = "events_address_group_member_id_seq", table = "events_sequence")
	private Long id;

	private AddressGroupMember()
	{
		super();
	}

	private AddressGroupMember(final AddressGroup addressGroup)
	{
		super();
		this.setAddressGroup(addressGroup);
	}

	private AddressGroupMember(final AddressGroup addressGroup, final Address address)
	{
		super();
		this.setAddressGroup(addressGroup);
		this.setAddress(address);
	}

	private AddressGroupMember(final AddressGroup addressGroup, final LinkPersonAddress link)
	{
		super();
		this.setAddressGroup(addressGroup);
		this.setLink(link);
	}

	public AddressGroupMember copy(final AddressGroup addressGroup)
	{
		AddressGroupMember copy = AddressGroupMember.newInstance(addressGroup);
		copy.setLink(this.getLink());
		copy.setAddress(this.getAddress());
		copy.setCopiedFrom(this.addressGroup);
		return copy;
	}

	public boolean isValid()
	{
		if (this.deleted) return false;
		if (this.link == null)
		{
			return this.address != null && this.address.isValid();
		}
		else
		{
			return this.link.isValid() && this.addressGroup.isValid();
		}
	}
	
	public boolean isValidLinkMember()
	{
		if (!this.deleted)
		{
			return this.link != null && this.link.isValid();
		}
		return false;
	}
	
	public boolean isValidAddressMember()
	{
		if (!this.deleted)
		{
			if (this.link == null || !this.link.isValid())
			{
				return this.address != null && this.address.isValid();
			}
		}
		return false;
	}
	
	public Long getAddressId()
	{
		return (this.address == null || !this.address.isValid()) ? ((this.link != null && this.link.isValid()) ? this.link.getAddress().getId() : null) : this.address.getId();
	}
	
	public Address getAddress()
	{
		return address;
	}

	public AddressGroup getAddressGroup()
	{
		return this.addressGroup;
	}

	public AddressGroup getCopiedFrom()
	{
		return this.copiedFrom;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public LinkPersonAddress getLink()
	{
		return this.link;
	}
	
	public void setAddressGroup(final AddressGroup addressGroup)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroup", this.addressGroup,
				this.addressGroup = addressGroup);
	}

	public void setCopiedFrom(final AddressGroup source)
	{
		this.propertyChangeSupport.firePropertyChange("copiedFrom", this.copiedFrom,
				this.copiedFrom = source);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("link", this.link, this.link = link);
	}

	public void setAddress(final Address address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	public static AddressGroupMember newInstance()
	{
		return (AddressGroupMember) AbstractEntity.newInstance(new AddressGroupMember());
	}

	public static AddressGroupMember newInstance(final AddressGroup addressGroup)
	{
		return (AddressGroupMember) AbstractEntity.newInstance(new AddressGroupMember(addressGroup));
	}

	public static AddressGroupMember newInstance(final AddressGroup addressGroup, final Address address)
	{
		return (AddressGroupMember) AbstractEntity.newInstance(new AddressGroupMember(addressGroup, address));
	}

	public static AddressGroupMember newInstance(final AddressGroup addressGroup, final LinkPersonAddress link)
	{
		return (AddressGroupMember) AbstractEntity.newInstance(new AddressGroupMember(addressGroup, link));
	}

}
