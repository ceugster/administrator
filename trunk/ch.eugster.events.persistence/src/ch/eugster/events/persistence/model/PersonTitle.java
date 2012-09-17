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
@Table(name = "events_person_title")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "person_title_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "person_title_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "person_title_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "person_title_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "person_title_version")) })
@Customizer(DeletedFilter.class)
public class PersonTitle extends AbstractEntity
{

	@Id
	@Column(name = "person_title_id")
	@GeneratedValue(generator = "events_person_title_id_seq")
	@TableGenerator(name = "events_person_title_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "person_title_title")
	private String title;

	@Override
	public Long getId()
	{
		return id;
	}

	public String getTitle()
	{
		return super.stringValueOf(title);
	}

	public void setTitle(String title)
	{
		this.propertyChangeSupport.firePropertyChange("title", this.title, this.title = title);
	}

	@Override
	public void setId(Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
		this.id = id;
	}

	public static PersonTitle newInstance()
	{
		return (PersonTitle) AbstractEntity.newInstance(new PersonTitle());
	}

}
