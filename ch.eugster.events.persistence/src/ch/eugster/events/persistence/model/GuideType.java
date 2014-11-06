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
@Table(name = "events_guide_type")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "guide_type_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "guide_type_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "guide_type_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "guide_type_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "guide_type_version")) })
public class GuideType extends AbstractEntity
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "guide_type_id")
	@GeneratedValue(generator = "events_guide_type_id_seq")
	@TableGenerator(name = "events_guide_type_id_seq", table = "events_sequence", initialValue = 20, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "guide_type_code")
	private String code;

	@Basic
	@Column(name = "guide_type_name")
	private String name;

	@Basic
	@Column(name = "guide_type_desc")
	private String description;

	@Basic
	@Column(name = "guide_type_template")
	private String template;

	@Basic
	@Column(name = "guide_type_use_in_prints")
	private boolean useInPrints;

	private GuideType()
	{
		super();
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(this.name);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
		this.code = code;
	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
		this.description = description;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setName(final String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public void setTemplate(String template) 
	{
		this.propertyChangeSupport.firePropertyChange("template", this.template, this.template = template);
	}

	public String getTemplate() {
		return template;
	}

	public void setUseInPrints(boolean useInPrints) 
	{
		this.propertyChangeSupport.firePropertyChange("useInPrints", this.useInPrints, this.useInPrints = useInPrints);
	}

	public boolean isUseInPrints() 
	{
		return useInPrints;
	}

	public static GuideType newInstance()
	{
		return (GuideType) AbstractEntity.newInstance(new GuideType());
	}

}
