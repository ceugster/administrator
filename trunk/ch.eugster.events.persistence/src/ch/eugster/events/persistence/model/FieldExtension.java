package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_field_extension")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "field_extension_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "field_extension_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "field_extension_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "field_extension_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "field_extension_version")) })
public class FieldExtension extends AbstractEntity
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "field_extension_id")
	@GeneratedValue(generator = "events_field_extension_id_seq")
	@TableGenerator(name = "events_field_extension_id_seq", table = "events_sequence", allocationSize = 1)
	private Long id;

	@Basic
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "field_extension_type")
	private FieldExtensionType fieldExtensionType;

	@Basic
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "field_extension_target")
	private FieldExtensionTarget target;

	@Basic
	@Column(name = "field_extension_label", columnDefinition = "VARCHAR(255)")
	private String label;

	@Basic
	@Column(name = "field_extension_max_val", columnDefinition = "INTEGER")
	private int maxValue;

	@Basic
	@Column(name = "field_extension_min_val", columnDefinition = "INTEGER")
	private int minValue;

	@Basic
	@Column(name = "field_extension_decimal", columnDefinition = "INTEGER")
	private int decimal;

	@Basic
	@Column(name = "field_extension_width_hint", columnDefinition = "INTEGER")
	private int widthHint;

	@Basic
	@Column(name = "field_extension_height_hint", columnDefinition = "INTEGER")
	private int heightHint;

	@Basic
	@Column(name = "field_extension_style", columnDefinition = "INTEGER")
	private int style;

	@Basic
	@Column(name = "field_extension_default_value")
	@Lob
	private String defaultValue;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "field_extension_searchable")
	private boolean searchable;

	protected FieldExtension()
	{
		super();
	}

	public void copy(final FieldExtension source)
	{
		this.setLabel(source.getLabel());
		this.setDefaultValue(source.getDefaultValue());
		this.setType(source.getType());
	}

	public int getDecimal()
	{
		return decimal;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public int getHeightHint()
	{
		return heightHint;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getLabel()
	{
		return label;
	}

	public int getMaxValue()
	{
		return maxValue;
	}

	public int getMinValue()
	{
		return minValue;
	}

	public int getStyle()
	{
		return style;
	}

	public FieldExtensionTarget getTarget()
	{
		return target;
	}

	public FieldExtensionType getType()
	{
		return this.fieldExtensionType;
	}

	public int getWidthHint()
	{
		return widthHint;
	}

	public boolean isSearchable()
	{
		return searchable;
	}

	public void setDecimal(final int decimal)
	{
		this.propertyChangeSupport.firePropertyChange("decimal", this.decimal, this.decimal = decimal);
	}

	public void setDefaultValue(final String defaultValue)
	{
		this.propertyChangeSupport.firePropertyChange("defaultValue", this.defaultValue,
				this.defaultValue = defaultValue);
	}

	public void setHeightHint(final int heightHint)
	{
		this.heightHint = heightHint;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLabel(final String label)
	{
		this.propertyChangeSupport.firePropertyChange("label", this.label, this.label = label);
	}

	public void setMaxValue(final int maxValue)
	{
		this.propertyChangeSupport.firePropertyChange("maxValue", this.maxValue, this.maxValue = maxValue);
	}

	public void setMinValue(final int minValue)
	{
		this.propertyChangeSupport.firePropertyChange("minValue", this.minValue, this.minValue = minValue);
	}

	public void setSearchable(final boolean searchable)
	{
		this.searchable = searchable;
	}

	public void setStyle(final int style)
	{
		this.style = style;
	}

	public void setTarget(final FieldExtensionTarget target)
	{
		this.propertyChangeSupport.firePropertyChange("target", this.target, this.target = target);
	}

	public void setType(final FieldExtensionType type)
	{
		this.propertyChangeSupport.firePropertyChange("fieldExtensionType", this.fieldExtensionType,
				this.fieldExtensionType = type);
	}

	public void setWidthHint(final int widthHint)
	{
		this.widthHint = widthHint;
	}

	public static FieldExtension newInstance()
	{
		return (FieldExtension) AbstractEntity.newInstance(new FieldExtension());
	}

}
