package ch.eugster.events.persistence.model;

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

@Entity
@Table(name = "events_bank_account")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "bank_account_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "bank_account_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "bank_account_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "bank_account_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "bank_account_version")) })
public class BankAccount extends AbstractEntity
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "bank_account_bank_id", referencedColumnName = "bank_id")
	private Bank bank;

	@ManyToOne
	@JoinColumn(name = "bank_account_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	@ManyToOne
	@JoinColumn(name = "bank_account_address_id", referencedColumnName = "address_id")
	private Address address;

	/*
	 * Data
	 */
	@Id
	@Column(name = "bank_account_id")
	@GeneratedValue(generator = "events_bank_account_id_seq")
	@TableGenerator(name = "events_bank_account_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "bank_account_number")
	private String accountNumber;

	@Basic
	@Column(name = "bank_account_iban")
	private String iban;

	private BankAccount()
	{
		super();
	}

	private BankAccount(final Address address)
	{
		super();
		this.setAddress(address);
	}

	private BankAccount(final LinkPersonAddress link)
	{
		super();
		this.setLink(link);
		this.setAddress(address);
	}

	public BankAccount copy()
	{
		BankAccount copy = BankAccount.newInstance(this.getLink());
		copy.setAddress(this.getAddress());
		copy.setAccountNumber(this.getAccountNumber());
		copy.setBank(this.getBank());
		copy.setIban(this.getIban());
		copy.setLink(this.getLink());
		return copy;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public Address getAddress()
	{
		if (this.link == null)
		{
			return this.address;
		}
		return this.link.getAddress();
	}

	public Bank getBank()
	{
		return bank;
	}

	public String getIban()
	{
		return iban;
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

	public void setAccountNumber(final String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public void setAddress(final Address address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	public void setBank(final Bank bank)
	{
		this.bank = bank;
	}

	public void setIban(final String iban)
	{
		this.iban = iban;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("link", this.link, this.link = link);
		this.setAddress(link.getAddress());
	}

	public static BankAccount newInstance()
	{
		return (BankAccount) AbstractEntity.newInstance(new BankAccount());
	}

	public static BankAccount newInstance(final Address address)
	{
		return (BankAccount) AbstractEntity.newInstance(new BankAccount(address));
	}

	public static BankAccount newInstance(final LinkPersonAddress link)
	{
		return (BankAccount) AbstractEntity.newInstance(new BankAccount(link));
	}

}
