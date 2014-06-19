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
		this.setParent(null, address);
	}

	private AddressGroupMember(final AddressGroup addressGroup, final LinkPersonAddress link)
	{
		super();
		this.setAddressGroup(addressGroup);
		this.setParent(link, link.getAddress());
	}

	public AddressGroupMember copy(final AddressGroup addressGroup)
	{
		AddressGroupMember copy = AddressGroupMember.newInstance(addressGroup);
		copy.setParent(this.getLink(), this.getAddress());
		copy.setCopiedFrom(this.addressGroup);
		return copy;
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
		this.copiedFrom = source;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setParent(final LinkPersonAddress link, final Address address)
	{
		this.link = link;
		this.address = address;
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
