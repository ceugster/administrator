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
@Table(name = "events_address_group_link")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "address_group_link_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "address_group_link_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "address_group_link_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "address_group_link_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "address_group_link_version")) })
public class AddressGroupLink extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "address_group_link_parent_id", referencedColumnName = "address_group_id")
	private AddressGroup parent = null;

	@ManyToOne()
	@JoinColumn(name = "address_group_link_child_id", referencedColumnName = "address_group_id")
	private AddressGroup child = null;

	/**
	 * Data
	 */
	@Id
	@Column(name = "address_group_link_id")
	@GeneratedValue(generator = "events_address_group_link_id_seq")
	@TableGenerator(name = "events_address_group_link_id_seq", table = "events_sequence")
	private Long id;

	private AddressGroupLink()
	{
		super();
	}

	private AddressGroupLink(final AddressGroup parent, final AddressGroup child)
	{
		super();
		this.setParent(parent);
		this.setChild(child);
	}

	@Override
	public AddressGroupLink clone()
	{
		AddressGroupLink link = AddressGroupLink.newInstance(this.parent, this.child);
		return link;
	}

	public AddressGroup getChild()
	{
		return this.child;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public AddressGroup getParent()
	{
		return this.parent;
	}

	public void setChild(final AddressGroup addressGroup)
	{
		this.propertyChangeSupport.firePropertyChange("child", this.child, this.child = addressGroup);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setParent(final AddressGroup addressGroup)
	{
		this.propertyChangeSupport.firePropertyChange("parent", this.parent, this.parent = addressGroup);
	}

	public static AddressGroupLink newInstance(final AddressGroup parent, final AddressGroup child)
	{
		return (AddressGroupLink) AbstractEntity.newInstance(new AddressGroupLink(parent, child));
	}
}
