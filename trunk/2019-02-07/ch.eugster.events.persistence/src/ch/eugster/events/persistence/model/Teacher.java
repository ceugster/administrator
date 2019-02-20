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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_teacher")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "teacher_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "teacher_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "teacher_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "teacher_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "teacher_version")) })
public class Teacher extends AbstractEntity implements LinkPersonAddressChild, ISelectedPhoneProvider,
		ISelectedEmailProvider
{
	@ManyToOne(optional = false)
	@JoinColumn(name = "teacher_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	/**
	 * Data
	 */
	@Id
	@Column(name = "teacher_id")
	@GeneratedValue(generator = "events_teacher_id_seq")
	@TableGenerator(name = "events_teacher_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "teacher_selected_phone")
	@Enumerated(EnumType.ORDINAL)
	private SelectedPhone selectedPhone;

	@Basic
	@Column(name = "teacher_best_reach_time")
	private String bestReachTime;

	@Basic
	@Column(name = "teacher_selected_email")
	@Enumerated(EnumType.ORDINAL)
	private SelectedEmail selectedEmail;

	@Basic
	@Column(name = "teacher_notes")
	private String notes;

	private Teacher()
	{
		super();
	}

	private Teacher(final LinkPersonAddress link)
	{
		super();
		this.setLink(link);
	}

	public void copy(final Teacher teacher)
	{
	}

	public boolean isValid()
	{
		return !this.deleted && this.getLink().isValid();
	}
	
	public String getBestReachTime()
	{
		return stringValueOf(this.bestReachTime);
	}

	@Override
	public String getEmail(final SelectedEmail email)
	{
		if (email == null)
		{
			return "";
		}
		switch (email)
		{
			case NONE:
			{
				return "";
			}
			case PERSON:
			{
				return this.link.getPerson().getEmail();
			}
			case LINK:
			{
				return this.link.getEmail();
			}
			case ADDRESS:
			{
				return this.link.getAddress().getEmail();
			}
		}
		return "";
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	@Override
	public LinkPersonAddress getLink()
	{
		return link;
	}

	@Override
	public String getPhone(final SelectedPhone phone)
	{
		if (phone == null)
		{
			return "";
		}
		switch (phone)
		{
			case NONE:
			{
				return "";
			}
			case PERSON:
			{
				return this.link.getPerson().getPhone();
			}
			case LINK:
			{
				return this.link.getPhone();
			}
			case ADDRESS:
			{
				return this.link.getAddress().getPhone();
			}
		}
		return "";
	}

	@Override
	public SelectedEmail getSelectedEmail()
	{
		return this.selectedEmail;
	}

	@Override
	public SelectedPhone getSelectedPhone()
	{
		return this.selectedPhone;
	}
	
	public String getNotes()
	{
		return stringValueOf(this.notes);
	}

	public void setBestReachTime(final String time)
	{
		this.propertyChangeSupport.firePropertyChange("bestReachTime", this.bestReachTime,
				this.bestReachTime = (time == null || time.isEmpty() ? null : time));
	}

	public void setNotes(final String notes)
	{
		this.propertyChangeSupport.firePropertyChange("notes", this.notes,
				this.notes = (notes == null || notes.isEmpty() ? null : notes));
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

	public void setSelectedEmail(final SelectedEmail email)
	{
		this.propertyChangeSupport.firePropertyChange("selectedEmail", this.selectedEmail, this.selectedEmail = email);
	}

	public void setSelectedPhone(final SelectedPhone selectedPhone)
	{
		this.propertyChangeSupport.firePropertyChange("selectedPhone", this.selectedPhone,
				this.selectedPhone = selectedPhone);
	}

	public static Teacher newInstance(final LinkPersonAddress link)
	{
		return (Teacher) AbstractEntity.newInstance(new Teacher(link));
	}
}
