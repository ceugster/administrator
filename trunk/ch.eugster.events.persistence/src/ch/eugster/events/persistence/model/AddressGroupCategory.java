package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

import java.util.Collection;
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

import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_address_group_category")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "address_group_category_user_id")) })
@AttributeOverrides({
		@AttributeOverride(name = "inserted", column = @Column(name = "address_group_category_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "address_group_category_updated")),
		@AttributeOverride(name = "calendar", column = @Column(name = "address_group_category_calendar")),
		@AttributeOverride(name = "deleted", column = @Column(name = "address_group_category_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "address_group_category_version")) })
@Customizer(DeletedFilter.class)
public class AddressGroupCategory extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "address_group_category_domain_id", referencedColumnName = "domain_id")
	private Domain domain = null;

	/**
	 * Data
	 */
	@Id
	@Column(name = "address_group_category_id")
	@GeneratedValue(generator = "events_address_group_category_id_seq")
	@TableGenerator(name = "events_address_group_category_id_seq", table = "events_sequence", initialValue = 10, allocationSize = 5)
	private Long id;

	/*
	 * Code
	 */
	@Basic
	@Column(name = "address_group_category_code")
	private String code;

	/*
	 * Name
	 */
	@Basic
	@Column(name = "address_group_category_name")
	private String name;

	/*
	 * Beschreibung
	 */
	@Basic
	@Column(name = "address_group_category_desc")
	private String description;

	/*
	 * Children
	 */
	@OneToMany(mappedBy = "addressGroupCategory", cascade = ALL)
	private final Collection<AddressGroup> addressGroups = new Vector<AddressGroup>();

	private AddressGroupCategory()
	{
	}

	private AddressGroupCategory(final Domain domain)
	{
		super();
		this.setDomain(domain);
	}

	/**
	 * 
	 * @param addressGroup
	 * @return <code>true</code> if either the new addressGroup is added or does
	 *         already exist in the list
	 */
	public void addAddressGroup(final AddressGroup addressGroup)
	{
		if (addressGroup == null)
			return;

		addressGroup.setAddressGroupCategory(this);
		this.addressGroups.add(addressGroup);
	}

	public AddressGroupCategory copy()
	{
		return copy(this.domain);
	}

	public AddressGroupCategory copy(final Domain domain)
	{
		AddressGroupCategory copy = AddressGroupCategory.newInstance(domain);
		copy.setCode(this.code);
		copy.setDescription(this.description);
		copy.setName(this.name);
		for (AddressGroup addressGroup : this.addressGroups)
		{
			addressGroup.copy(copy);
		}
		return copy;
	}

	public Collection<AddressGroup> getAddressGroups()
	{
		return this.addressGroups;
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	public Domain getDomain()
	{
		return this.domain;
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

	/**
	 * 
	 * @param addressGroup
	 * @return the addressGroup that has been removed or null if no addressGroup
	 *         has been removed. The addressGroups id and reference to the
	 *         addressGroupCategory object is set to <code>null</code>
	 */
	public void removeAddressGroup(final AddressGroup addressGroup)
	{
		this.addressGroups.remove(addressGroup);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		this.deleted = deleted;
		for (AddressGroup addressGroup : this.addressGroups)
			addressGroup.setDeleted(deleted);
	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
	}

	public void setDomain(final Domain domain)
	{
		this.propertyChangeSupport.firePropertyChange("domain", this.domain, this.domain = domain);
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

	public static AddressGroupCategory newInstance(final Domain domain)
	{
		return (AddressGroupCategory) AbstractEntity.newInstance(new AddressGroupCategory(domain));
	}
}
