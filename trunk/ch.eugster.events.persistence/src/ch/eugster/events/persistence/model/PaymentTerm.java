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
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_payment_term")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "payment_term_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "payment_term_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "payment_term_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "payment_term_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "payment_term_version")) })
public class PaymentTerm extends AbstractEntity
{
	/**
	 * Data
	 */
	@Id
	@Column(name = "payment_term_id")
	@GeneratedValue(generator = "events_payment_term_id_seq")
	@TableGenerator(name = "events_payment_term_id_seq", table = "events_sequence", initialValue = 2000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "payment_term_text")
	private String text;

	private PaymentTerm()
	{
		super();
	}

	public PaymentTerm copy()
	{
		PaymentTerm copy = PaymentTerm.newInstance();
		copy.setText(this.getText());
		return copy;
	}

	public String getText()
	{
		return PaymentTerm.stringValueOf(this.text);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public void setText(final String text)
	{
		this.propertyChangeSupport.firePropertyChange("text", this.text, this.text = text);
	}

	@Override
	public void setId(final Long id)
	{
		this.id = id;
	}

	public static PaymentTerm newInstance()
	{
		return (PaymentTerm) AbstractEntity.newInstance(new PaymentTerm());
	}

}
