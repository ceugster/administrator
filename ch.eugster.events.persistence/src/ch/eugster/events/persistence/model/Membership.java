package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

import java.util.List;
import java.util.Vector;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_membership")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "membership_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "membership_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "membership_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "membership_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "membership_version")) })
public class Membership extends AbstractEntity
{
	@Id
	@Column(name = "membership_id")
	@GeneratedValue(generator = "events_membership_id_seq")
	@TableGenerator(name = "events_membership_id_seq", table = "events_sequence", initialValue = 0, allocationSize = 5)
	private Long id;

	/*
	 * Code
	 */
	@Basic
	@Column(name = "membership_code")
	private String code;

	/*
	 * Name
	 */
	@Basic
	@Column(name = "membership_name")
	private String name;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "membership_with_code")
	private boolean memberCodeMandatory;

	@OneToOne(cascade = CascadeType.ALL, optional = true)
	@JoinColumn(name = "membership_address_id", referencedColumnName = "address_id")
	private Address address;

	/*
	 * Children
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "membership", cascade = ALL)
	private final List<Member> members = new Vector<Member>();

	private Membership()
	{
		super();
	}

	private Membership(final AddressGroupCategory addressGroupCategory)
	{
		super();
	}

	/**
	 * 
	 * @param member
	 * @return <code>true</code> if either a new member is added or a member
	 *         representing the person is already in the list
	 */
	public void addMember(final Member member)
	{
		this.propertyChangeSupport.firePropertyChange("members", this.members, this.members.add(member));
	}

	public String format()
	{
		StringBuilder builder = new StringBuilder();
		if (getCode().length() > 0 && !getCode().equals(getName()))
		{
			builder = builder.append(getCode() + " - ");
		}
		return builder.append(this.getName()).toString();
	}

	public Address getAddress()
	{
		return address;
	}

	public String getCode()
	{
		return stringValueOf(this.code);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public Member getMember(final Address address)
	{
		if (address != null)
		{
			for (Member member : members)
			{
				if (member.getAddress().getId().equals(address.getId()))
				{
					return member;
				}
			}
		}
		return null;
	}

	public List<Member> getMembers()
	{
		return this.members;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(this.name);
	}

	public boolean isMemberCodeMandatory()
	{
		return memberCodeMandatory;
	}

	/**
	 * 
	 * @param member
	 * @return the member that has been removed or null if no member has been
	 *         removed. The members id and reference to the addressGroup object
	 *         is set to <code>null</code>
	 */
	public void removeMember(final AddressGroupMember member)
	{
		this.propertyChangeSupport.firePropertyChange("members", this.members, this.members.remove(member));
	}

	public void setAddress(final Address address)
	{
		this.address = address;
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		super.setDeleted(deleted);
		for (Member member : this.members)
			member.setDeleted(deleted);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setMemberCodeMandatory(final boolean withCode)
	{
		this.memberCodeMandatory = withCode;
	}

	public void setName(final String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public static Membership newInstance()
	{
		return (Membership) AbstractEntity.newInstance(new Membership());
	}
}
