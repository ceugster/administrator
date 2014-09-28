package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_address_group")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "address_group_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "address_group_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "address_group_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "address_group_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "address_group_version")) })
public class AddressGroup extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne(cascade = { PERSIST, MERGE, REFRESH }, optional = false)
	@JoinColumn(name = "address_group_address_group_category_id", referencedColumnName = "address_group_category_id")
	private AddressGroupCategory addressGroupCategory;

	@Id
	@Column(name = "address_group_id")
	@GeneratedValue(generator = "events_address_group_id_seq")
	@TableGenerator(name = "events_address_group_id_seq", table = "events_sequence", initialValue = 2000, allocationSize = 5)
	private Long id;

	/**
	 * Data
	 */
	/*
	 * Code
	 */
	@Basic
	@Column(name = "address_group_code")
	private String code;

	/*
	 * Name
	 */
	@Basic
	@Column(name = "address_group_name")
	private String name;

	/*
	 * Beschreibung
	 */
	@Basic
	@Column(name = "address_group_desc")
	private String description;

	/*
	 * AddressGroupMembers
	 */
	@OneToMany(mappedBy = "addressGroup", cascade = ALL)
	private final List<AddressGroupMember> addressGroupMembers = new Vector<AddressGroupMember>();

	// /*
	// * Parents
	// */
	// @OneToMany(mappedBy = "parent", cascade = ALL)
	// private final Collection<AddressGroupLink> parents = new
	// Vector<AddressGroupLink>();

	// /*
	// * Children
	// */
	// @OneToMany(mappedBy = "parent", cascade = ALL)
	// private final Collection<AddressGroupLink> children = new
	// Vector<AddressGroupLink>();

	private AddressGroup()
	{
		super();
	}

	private AddressGroup(final AddressGroupCategory category)
	{
		super();
		this.setAddressGroupCategory(category);
	}

	// public void addChild(final AddressGroupLink child)
	// {
	// this.propertyChangeSupport.firePropertyChange("addChild", this.children,
	// this.children.add(child));
	// }

	/**
	 * 
	 * @param addressGroupMember
	 * @return <code>true</code> if either a new addressGroupMember is added or
	 *         a addressGroupMember representing the person is already in the
	 *         list
	 */
	public void addAddressGroupMember(final AddressGroupMember addressGroupMember)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers.add(addressGroupMember));
	}

	public AddressGroup copy(final AddressGroupCategory category)
	{
		AddressGroup copy = AddressGroup.newInstance(category);
		copy.setCode(this.getCode());
		copy.setDescription(this.getDescription());
		copy.setName(this.getName());
		for (AddressGroupMember addressGroupMember : this.addressGroupMembers)
		{
			copy.addAddressGroupMember(addressGroupMember.copy(copy));
		}
		return copy;
	}

	public AddressGroupCategory getAddressGroupCategory()
	{
		return this.addressGroupCategory;
	}

	// public Collection<AddressGroupLink> getChildren()
	// {
	// return children;
	// }

	public List<AddressGroupMember> getAddressGroupMembers()
	{
		return this.addressGroupMembers;
	}

	public List<AddressGroupMember> getValidAddressGroupMembers()
	{
		List<AddressGroupMember> validMembers = new ArrayList<AddressGroupMember>();
		for (AddressGroupMember member : this.addressGroupMembers)
		{
			if (!member.isDeleted() && !member.getAddress().isDeleted() && (member.getLink() == null || !member.getLink().isDeleted()))
			{
				validMembers.add(member);
			}
		}
		return validMembers;
	}

	public String getCode()
	{
		return stringValueOf(this.code);
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
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

	// public Collection<AddressGroupLink> getParents()
	// {
	// return parents;
	// }

	// public void removeChild(final AddressGroupLink child)
	// {
	// this.propertyChangeSupport.firePropertyChange("removeChild",
	// this.children, this.children.remove(child));
	// }

	/**
	 * 
	 * @param addressGroupMember
	 * @return the addressGroupMember that has been removed or null if no
	 *         addressGroupMember has been removed. The addressGroupMembers id
	 *         and reference to the addressGroup object is set to
	 *         <code>null</code>
	 */
	public void removeAddressGroupMember(final AddressGroupMember addressGroupMember)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupMembers", this.addressGroupMembers,
				this.addressGroupMembers.remove(addressGroupMember));
	}

	// public void removeParent(final AddressGroupLink parent)
	// {
	// this.propertyChangeSupport.firePropertyChange("removeParent",
	// this.parents, this.parents.remove(parent));
	// }

	public void setAddressGroupCategory(final AddressGroupCategory addressGroupCategory)
	{
		this.propertyChangeSupport.firePropertyChange("addressGroupCategory", this.addressGroupCategory,
				this.addressGroupCategory = addressGroupCategory);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}
	
	public boolean contains(LinkPersonAddress link, Address address)
	{
		List<AddressGroupMember> members = this.getValidAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (member.getAddress().getId().equals(address.getId()))
			{
				if (member.getLink() != null && member.getLink().getId().equals(link.getId()))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		super.setDeleted(deleted);
		for (AddressGroupMember addressGroupMember : this.addressGroupMembers)
		{
			addressGroupMember.setDeleted(deleted);
		}
		// for (AddressGroupLink parent : this.parents)
		// {
		// parent.setDeleted(deleted);
		// }
		// for (AddressGroupLink child : this.children)
		// {
		// child.setDeleted(deleted);
		// }
	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
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

	public static AddressGroup newInstance(final AddressGroupCategory addressGroupCategory)
	{
		return (AddressGroup) AbstractEntity.newInstance(new AddressGroup(addressGroupCategory));
	}
}
