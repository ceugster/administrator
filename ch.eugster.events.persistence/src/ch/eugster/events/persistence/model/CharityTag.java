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
@Table(name = "events_charity_tag")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "charity_tag_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "charity_tag_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "charity_tag_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "charity_tag_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "charity_tag_version")) })
public class CharityTag extends AbstractEntity
{
	@Id
	@Column(name = "charity_tag_id")
	@GeneratedValue(generator = "events_charity_tag_id_seq")
	@TableGenerator(name = "events_charity_tag_id_seq", table = "events_sequence")
	private Long id;


	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "charity_tag_charity_run_id", referencedColumnName = "charity_run_id")
	private CharityRun charityRun;

	@Basic
	@Column(name = "charity_tag_tag_id")
	private String tagId;

	@Basic
	@Column(name = "charity_tag_start_number")
	private Long startNumber;

	private CharityTag()
	{
		super();
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	@Override
	public void setId(Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void copy(CharityTag schoolClass)
	{
	}

	public void setTagId(String tagId)
	{
		this.propertyChangeSupport.firePropertyChange("tagId", this.tagId,
				this.tagId = tagId == null || tagId.isEmpty() ? null : tagId);
	}

	public String getTagId()
	{
		return this.tagId;
	}
	
	public void setStartNumber(Long startNumber)
	{
		this.propertyChangeSupport.firePropertyChange("startNumber", this.startNumber, this.startNumber = startNumber);
	}

	public Long getStartNumber()
	{
		return this.startNumber;
	}
	
	public static CharityTag newInstance()
	{
		return (CharityTag) AbstractEntity.newInstance(new CharityTag());
	}
}
