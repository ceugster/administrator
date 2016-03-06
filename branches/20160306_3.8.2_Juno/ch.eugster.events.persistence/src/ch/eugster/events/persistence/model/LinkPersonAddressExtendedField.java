package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_extended_field")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "extended_field_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "extended_field_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "extended_field_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "extended_field_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "extended_field_version")) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "extended_field_discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("L")
public class LinkPersonAddressExtendedField extends AbstractEntity implements ExtendedField
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "extended_field_owner_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	/**
	 * Data
	 */
	@Id
	@Column(name = "extended_field_id")
	@GeneratedValue(generator = "events_extended_field_id_seq")
	@TableGenerator(name = "events_extended_field_id_seq", table = "events_sequence", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "extended_field_field_extension_id", referencedColumnName = "field_extension_id")
	private FieldExtension fieldExtension;

	@Basic
	@Column(name = "extended_field_value")
	private String value;

	private LinkPersonAddressExtendedField()
	{
	}

	private LinkPersonAddressExtendedField(final LinkPersonAddress link, final FieldExtension extension)
	{
		super();
		this.setLink(link);
		this.setFieldExtension(extension);
	}

	@Override
	public FieldExtension getFieldExtension()
	{
		return fieldExtension;
	}

	@Override
	public Long getId()
	{
		return id;
	}

	public LinkPersonAddress getLink()
	{
		return this.link;
	}

	public String getValue()
	{
		return value;
	}

	public void setFieldExtension(final FieldExtension fieldExtension)
	{
		this.propertyChangeSupport.firePropertyChange("fieldExtension", this.fieldExtension,
				this.fieldExtension = fieldExtension);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLink(final LinkPersonAddress link)
	{
		this.propertyChangeSupport.firePropertyChange("link", this.link, this.link = link);
	}

	public void setValue(final String value)
	{
		this.propertyChangeSupport.firePropertyChange("value", this.value, this.value = value);
	}

	public static ExtendedField newInstance(final LinkPersonAddress link, final FieldExtension extension)
	{
		return (LinkPersonAddressExtendedField) AbstractEntity.newInstance(new LinkPersonAddressExtendedField(link,
				extension));
	}
}
