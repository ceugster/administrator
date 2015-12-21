package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
	@ManyToOne(optional=true, cascade=CascadeType.ALL)
	@JoinColumn(name = "bank_account_bank_id", referencedColumnName = "bank_id")
	private Bank bank;

	/*
	 * if person is account owner, use following reference
	 */
	@ManyToOne(optional=true, cascade=CascadeType.ALL)
	@JoinColumn(name = "bank_account_person_id", referencedColumnName = "person_id")
	private Person person;

	/*
	 * if not a person but an address use following reference
	 */
	@ManyToOne(optional=true, cascade=CascadeType.ALL)
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

	private BankAccount(final Person person)
	{
		super();
		this.setPerson(person);
	}

	public BankAccount copy()
	{
		BankAccount copy = BankAccount.newInstance();
		copy.setPerson(this.getPerson());
		copy.setAddress(this.getAddress());
		copy.setAccountNumber(this.getAccountNumber());
		copy.setBank(this.getBank());
		copy.setIban(this.getIban());
		return copy;
	}

	public String getAccountNumber()
	{
		return AbstractEntity.stringValueOf(accountNumber);
	}

	public Address getAddress()
	{
		if (this.person == null || (this.person.isDeleted()))
		{
			return this.address;
		}
		return null;
	}

	public Bank getBank()
	{
		return bank;
	}

	public String getIban()
	{
		return AbstractEntity.stringValueOf(iban);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public Person getPerson()
	{
		if (this.address == null || (this.address.isDeleted()))
		{
			return this.person;
		}
		return null;
	}

	public void setAccountNumber(final String accountNumber)
	{
		this.propertyChangeSupport.firePropertyChange("accountNumber", this.accountNumber, this.accountNumber = accountNumber);
	}

	public void setAddress(final Address address)
	{
		this.propertyChangeSupport.firePropertyChange("address", this.address, this.address = address);
	}

	public void setBank(final Bank bank)
	{
		this.propertyChangeSupport.firePropertyChange("bank", this.bank, this.bank = bank);
	}

	public void setIban(final String iban)
	{
		this.propertyChangeSupport.firePropertyChange("iban", this.iban, this.iban = iban);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setPerson(final Person person)
	{
		this.propertyChangeSupport.firePropertyChange("person", this.person, this.person = person);
	}

	public static BankAccount newInstance()
	{
		return (BankAccount) AbstractEntity.newInstance(new BankAccount());
	}

	public static BankAccount newInstance(final Address address)
	{
		return (BankAccount) AbstractEntity.newInstance(new BankAccount(address));
	}

	public static BankAccount newInstance(final Person person)
	{
		return (BankAccount) AbstractEntity.newInstance(new BankAccount(person));
	}

}
