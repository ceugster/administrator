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

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_address_salutation")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "address_salutation_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "address_salutation_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "address_salutation_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "address_salutation_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "address_salutation_version")) })
@Customizer(DeletedFilter.class)
public class AddressSalutation extends AbstractEntity
{
	@Id
	@Column(name = "address_salutation_id")
	@GeneratedValue(generator = "events_address_salutation_id_seq")
	@TableGenerator(name = "events_address_salutation_id_seq", table = "events_sequence", allocationSize = 1)
	private Long id;

	@Basic
	@Column(name = "address_salutation_salutation")
	private String salutation;

	@Basic
	@Column(name = "address_salutation_polite")
	private String polite;

	@Basic
	@Column(name = "address_salutation_show_address_name_for_person")
	@Convert("booleanConverter")
	private boolean showAddressNameForPersons;

	@Override
	public Long getId()
	{
		return id;
	}

	public String getPolite()
	{
		return AbstractEntity.stringValueOf(polite);
	}

	public String getSalutation()
	{
		return AbstractEntity.stringValueOf(salutation);
	}

	public boolean isShowAddressNameForPersons()
	{
		return showAddressNameForPersons;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
		this.id = id;
	}

	public void setPolite(final String polite)
	{
		this.propertyChangeSupport.firePropertyChange("polite", this.polite, this.polite = polite);
	}

	public void setSalutation(final String salutation)
	{
		this.propertyChangeSupport.firePropertyChange("salutation", this.salutation, this.salutation = salutation);
	}

	public void setShowAddressNameForPersons(final boolean showAddressNameForPersons)
	{
		this.propertyChangeSupport.firePropertyChange("showAddressNameForPersons", this.showAddressNameForPersons,
				this.showAddressNameForPersons = showAddressNameForPersons);
	}

	public static AddressSalutation newInstance()
	{
		return new AddressSalutation();
	}
}
