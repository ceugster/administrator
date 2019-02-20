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
@Table(name = "events_visit_visitor")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "visit_visitor_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "visit_visitor_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "visit_visitor_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "visit_visitor_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "visit_visitor_version")) })
public class VisitVisitor extends AbstractEntity
{
	@ManyToOne
	@JoinColumn(name = "visit_visitor_visit_id", referencedColumnName = "visit_id")
	private Visit visit;

	@ManyToOne
	@JoinColumn(name = "visit_visitor_visitor_id", referencedColumnName = "visitor_id")
	private Visitor visitor;

	/*
	 * Data
	 */
	@Id
	@Column(name = "visit_visitor_id")
	@GeneratedValue(generator = "events_visit_visitor_id_seq")
	@TableGenerator(name = "events_visit_visitor_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "visit_visitor_type")
	@Enumerated(EnumType.ORDINAL)
	private VisitorType type;

	@Basic
	@Column(name = "visit_visitor_selected_phone")
	@Enumerated(EnumType.ORDINAL)
	private SelectedPhone selectedPhone;

	@Basic
	@Column(name = "visit_visitor_selected_email")
	@Enumerated(EnumType.ORDINAL)
	private SelectedEmail selectedEmail;

	private VisitVisitor()
	{
		super();
	}

	private VisitVisitor(Visit visit, Visitor visitor)
	{
		super();
		this.setVisit(visit);
		this.setVisitor(visitor);
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

	public void setVisit(Visit visit)
	{
		this.propertyChangeSupport.firePropertyChange("visit", this.visit, this.visit = visit);
	}

	public Visit getVisit()
	{
		return visit;
	}

	public void setSelectedPhone(SelectedPhone selectedPhone)
	{
		this.propertyChangeSupport.firePropertyChange("selectedPhone", this.selectedPhone,
				this.selectedPhone = selectedPhone);
	}

	public SelectedPhone getSelectedPhone()
	{
		return this.selectedPhone == null ? SelectedPhone.NONE : this.selectedPhone;
	}

	public void setSelectedEmail(SelectedEmail selectedEmail)
	{
		this.propertyChangeSupport.firePropertyChange("selectedEmail", this.selectedEmail,
				this.selectedEmail = selectedEmail);
	}

	public SelectedEmail getSelectedEmail()
	{
		return this.selectedEmail == null ? SelectedEmail.NONE : this.selectedEmail;
	}

	public String getPhone()
	{
		if (visitor == null || selectedPhone == null || selectedPhone.equals(SelectedPhone.NONE))
		{
			return "";
		}
		if (selectedPhone.equals(SelectedPhone.LINK))
		{
			return this.getVisitor().getLink().getPhone();
		}
		if (selectedPhone.equals(SelectedPhone.PERSON))
		{
			return this.getVisitor().getLink().getPerson().getPhone();
		}
		if (selectedPhone.equals(SelectedPhone.ADDRESS))
		{
			return this.getVisitor().getLink().getAddress().getPhone();
		}
		return "";
	}

	public String getEmail(SelectedEmail selectedEmail)
	{
		if (visitor == null || selectedEmail == null || selectedEmail.equals(SelectedEmail.NONE))
		{
			return "";
		}
		if (selectedEmail.equals(SelectedEmail.LINK))
		{
			return this.getVisitor().getLink().getEmail();
		}
		if (selectedEmail.equals(SelectedEmail.PERSON))
		{
			return this.getVisitor().getLink().getPerson().getEmail();
		}
		if (selectedEmail.equals(SelectedEmail.ADDRESS))
		{
			return this.getVisitor().getLink().getAddress().getEmail();
		}
		return "";
	}

	public static VisitVisitor newInstance()
	{
		return (VisitVisitor) AbstractEntity.newInstance(new VisitVisitor());
	}

	public static VisitVisitor newInstance(Visit visit, Visitor visitor)
	{
		return (VisitVisitor) AbstractEntity.newInstance(new VisitVisitor(visit, visitor));
	}

	public void setType(VisitorType type)
	{
		this.type = type;
	}

	public VisitorType getType()
	{
		return type;
	}

	public void setVisitor(Visitor visitor)
	{
		this.propertyChangeSupport.firePropertyChange("visitor", this.visitor, this.visitor = visitor);
	}

	public Visitor getVisitor()
	{
		return visitor;
	}

	public enum VisitorType
	{
		VISITOR, TRAINEE;

		public String label()
		{
			switch (this)
			{
				case VISITOR:
				{
					return "Schulbesucher/in";
				}
				case TRAINEE:
				{
					return "Hospitant/in";
				}
				default:
				{
					return "";
				}
			}
		}
	}
}
