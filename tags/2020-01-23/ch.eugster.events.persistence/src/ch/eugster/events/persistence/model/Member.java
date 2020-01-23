package ch.eugster.events.persistence.model;

import java.util.Calendar;

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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "events_member")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "member_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "member_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "member_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "member_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "member_version")) })
public class Member extends AbstractEntity
{
	@ManyToOne(optional = false)
	@JoinColumn(name = "member_address_id", referencedColumnName = "address_id")
	private Address address;

	@ManyToOne(optional = true)
	@JoinColumn(name = "member_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	@ManyToOne(optional = false)
	@JoinColumn(name = "member_membership_id", referencedColumnName = "membership_id")
	private Membership membership;

	/*
	 * Data
	 */
	@Id
	@Column(name = "member_id")
	@GeneratedValue(generator = "events_member_id_seq")
	@TableGenerator(name = "events_member_id_seq", table = "events_sequence", initialValue = 50000, allocationSize = 5)
	private Long id;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "member_date")
	private Calendar date;

	@Basic
	@Column(name = "member_code")
	private String code;

	private Member()
	{
		super();
	}

	private Member(final Membership membership)
	{
		super();
		this.setMembership(membership);
	}

	private Member(final Membership membership, final Address address)
	{
		super();
		this.setAddress(address);
		this.setMembership(membership);
	}

	private Member(final Membership membership, final LinkPersonAddress link)
	{
		super();
		this.setLink(link);
		this.setMembership(membership);
	}

	public boolean isValidLink()
	{
		return !this.deleted && this.link.isValid();
	}
	
	public boolean isValidAddress()
	{
		return !this.deleted && this.address.isValid();
	}
	
	public Address getAddress()
	{
		return this.address;
	}

	public String getCode()
	{
		return stringValueOf(code);
	}

	public Calendar getDate()
	{
		return this.date;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public LinkPersonAddress getLink()
	{
		return link;
	}

	public Membership getMembership()
	{
		return this.membership;
	}

	public void setAddress(final Address address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	public void setCode(final String code)
	{
		this.code = code;
	}

	public void setDate(final Calendar date)
	{
		this.propertyChangeSupport.firePropertyChange("date", this.date, this.date = date);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLink(final LinkPersonAddress link)
	{
		this.link = link;
		this.setAddress(link.getAddress());
	}

	public void setMembership(final Membership membership)
	{
		this.propertyChangeSupport.firePropertyChange("membership", this.membership, this.membership = membership);
	}

	public static Member newInstance()
	{
		return (Member) AbstractEntity.newInstance(new Member());
	}

	public static Member newInstance(final Membership membership)
	{
		return (Member) AbstractEntity.newInstance(new Member(membership));
	}

	public static Member newInstance(final Membership membership, final Address address)
	{
		return (Member) AbstractEntity.newInstance(new Member(membership, address));
	}

	public static Member newInstance(final Membership membership, final LinkPersonAddress link)
	{
		return (Member) AbstractEntity.newInstance(new Member(membership, link));
	}
}
