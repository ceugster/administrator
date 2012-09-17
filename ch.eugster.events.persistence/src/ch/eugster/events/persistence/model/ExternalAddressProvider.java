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

import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_external_address_provider")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "external_address_provider_user_id")) })
@AttributeOverrides({
		@AttributeOverride(name = "inserted", column = @Column(name = "external_address_provider_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "external_address_provider_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "external_address_provider_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "external_address_provider_version")) })
@Customizer(DeletedFilter.class)
public class ExternalAddressProvider extends AbstractEntity
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "external_address_provider_id")
	@GeneratedValue(generator = "events_external_address_provider_id_seq")
	@TableGenerator(name = "events_external_address_provider_id_seq", table = "events_sequence", initialValue = 1, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "external_address_provider_name")
	private String name;

	@Override
	public Long getId()
	{
		return id;
	}

	@Override
	public void setId(Long id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public static ExternalAddressProvider newInstance()
	{
		return (ExternalAddressProvider) AbstractEntity.newInstance(new ExternalAddressProvider());
	}
}
