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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_visitor")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "visitor_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "visitor_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "visitor_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "visitor_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "visitor_version")) })
public class Visitor extends AbstractEntity implements LinkPersonAddressChild, ISelectedPhoneProvider,
		ISelectedEmailProvider
{
	@ManyToOne
	@JoinColumn(name = "visitor_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	/*
	 * Data
	 */
	@Id
	@Column(name = "visitor_id")
	@GeneratedValue(generator = "events_visitor_id_seq")
	@TableGenerator(name = "events_visitor_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "visitor_selected_phone")
	private SelectedPhone selectedPhone;

	@Basic
	@Column(name = "visitor_selected_email")
	private SelectedEmail selectedEmail;

	private Visitor()
	{
		super();
	}

	private Visitor(final LinkPersonAddress link)
	{
		super();
		this.setLink(link);
	}

	public void copy(final Visitor source)
	{
		this.setLink(source.getLink());
	}

	@Override
	public String getEmail(final SelectedEmail selectedEmail)
	{
		if (selectedEmail == null || selectedEmail.equals(SelectedEmail.NONE))
		{
			return "";
		}
		if (selectedEmail.equals(SelectedEmail.LINK))
		{
			return this.getLink().getEmail();
		}
		if (selectedEmail.equals(SelectedEmail.PERSON))
		{
			return this.getLink().getPerson().getEmail();
		}
		if (selectedEmail.equals(SelectedEmail.ADDRESS))
		{
			return this.getLink().getAddress().getEmail();
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
		return this.link;
	}

	@Override
	public String getPhone(final SelectedPhone selectedPhone)
	{
		if (selectedPhone == null || selectedPhone.equals(SelectedPhone.NONE))
		{
			return "";
		}
		if (selectedPhone.equals(SelectedPhone.LINK))
		{
			return this.getLink().getPhone();
		}
		if (selectedPhone.equals(SelectedPhone.PERSON))
		{
			return this.getLink().getPerson().getPhone();
		}
		if (selectedPhone.equals(SelectedPhone.ADDRESS))
		{
			return this.getLink().getAddress().getPhone();
		}
		return "";
	}

	@Override
	public SelectedEmail getSelectedEmail()
	{
		return this.selectedEmail == null ? SelectedEmail.NONE : this.selectedEmail;
	}

	@Override
	public SelectedPhone getSelectedPhone()
	{
		return this.selectedPhone == null ? SelectedPhone.NONE : this.selectedPhone;
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

	public void setSelectedEmail(final SelectedEmail selectedEmail)
	{
		this.propertyChangeSupport.firePropertyChange("selectedEmail", this.selectedEmail,
				this.selectedEmail = selectedEmail);
	}

	public void setSelectedPhone(final SelectedPhone selectedPhone)
	{
		this.propertyChangeSupport.firePropertyChange("selectedPhone", this.selectedPhone,
				this.selectedPhone = selectedPhone);
	}

	public static Visitor newInstance()
	{
		return (Visitor) AbstractEntity.newInstance(new Visitor());
	}

	public static Visitor newInstance(final LinkPersonAddress link)
	{
		return (Visitor) AbstractEntity.newInstance(new Visitor(link));
	}

}