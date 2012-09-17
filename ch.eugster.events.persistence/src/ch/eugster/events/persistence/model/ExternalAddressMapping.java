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

import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_pa_link")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "external_address_mapping_user_id")) })
@AttributeOverrides({
		@AttributeOverride(name = "inserted", column = @Column(name = "external_address_mapping_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "external_address_mapping_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "external_address_mapping_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "external_address_mapping_version")) })
@Customizer(DeletedFilter.class)
public class ExternalAddressMapping extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "external_address_mapping_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "external_address_mapping_external_address_provider_id", referencedColumnName = "external_address_provider_id")
	private ExternalAddressProvider externalProvider;

	/**
	 * Data
	 */
	@Id
	@Column(name = "external_address_mapping_id")
	@GeneratedValue(generator = "events_external_address_mapping_id_seq")
	@TableGenerator(name = "events_external_address_mapping_id_seq", table = "events_sequence", initialValue = 50000, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "external_address_mapping_external_id")
	private String externalId;

	private ExternalAddressMapping()
	{
		super();
	}

	public void copy(final ExternalAddressMapping source)
	{
		this.setLink(source.getLink());
		this.setExternalProvider(source.getExternalProvider());
	}

	public String getExternalId()
	{
		return externalId;
	}

	public ExternalAddressProvider getExternalProvider()
	{
		return externalProvider;
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

	public void setExternalId(final String externalId)
	{
		this.externalId = externalId;
	}

	public void setExternalProvider(final ExternalAddressProvider externalProvider)
	{
		this.externalProvider = externalProvider;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLink(final LinkPersonAddress link)
	{
		this.link = link;
	}

	public static ExternalAddressMapping newInstance()
	{
		return (ExternalAddressMapping) AbstractEntity.newInstance(new ExternalAddressMapping());
	}

}
