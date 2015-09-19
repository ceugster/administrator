package ch.eugster.events.persistence.model;

import java.util.Calendar;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_participant")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "participant_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "participant_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "participant_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "participant_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "participant_version")) })
public class Participant extends AbstractEntity
{
	@ManyToOne
	@JoinColumn(name = "participant_pa_link_id", referencedColumnName = "pa_link_id")
	private LinkPersonAddress link;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "participant_booking_id", referencedColumnName = "booking_id")
	private Booking booking;

	@ManyToOne
	@JoinColumn(name = "participant_booking_type_id", referencedColumnName = "booking_type_id")
	private BookingType bookingType;

	/*
	 * Data
	 */
	@Id
	@Column(name = "participant_id")
	@GeneratedValue(generator = "events_participant_id_seq")
	@TableGenerator(name = "events_participant_id_seq", table = "events_sequence", initialValue = 10000, allocationSize = 5)
	private Long id;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "participant_date")
	private Calendar date;

	@Basic
	@Column(name = "participant_count")
	private int count = 1;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "participant_free")
	private boolean free = false;

	private Participant()
	{
		super();
	}

	private Participant(final Booking booking)
	{
		super();
		this.setBooking(booking);
	}

	private Participant(final LinkPersonAddress link, final Booking booking)
	{
		super();
		this.setLink(link);
		this.setBooking(booking);
	}

	public Booking getBooking()
	{
		return this.booking;
	}

	public BookingType getBookingType()
	{
		return this.bookingType;
	}

	public int getCount()
	{
		return count;
	}
	
	public double getPrice()
	{
		return this.isFree() || this.bookingType == null ? 0d : this.bookingType.getPrice();
	}

	public double getAmount()
	{
		return this.getPrice() * this.count;
	}
	
	public Calendar getDate()
	{
		return this.date;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getEmail()
	{
		String email = this.link.getPerson().getEmail();
		if (email.isEmpty())
		{
			email = this.link.getEmail();
			if (email.isEmpty())
			{
				email = this.link.getAddress().getEmail();
			}
		}
		return email;
	}
	
	public LinkPersonAddress getLink()
	{
		return this.link;
	}

	// public boolean isCorrespondent() {
	// if (this.booking.getParticipant() == null) return false;
	// if (this.booking.getParticipant().getId() == null || this.getId() ==
	// null)
	// return
	// this.booking.getParticipant().getPerson().equals(this.getPerson());
	// else
	// return this.booking.getParticipant().getId().equals(this.getId());
	// }
	//
	// public void setCorrespondent(boolean correspondent) {
	// this.booking.setParticipant(this);
	// }

	public void setBooking(final Booking booking)
	{
		this.propertyChangeSupport.firePropertyChange("booking", this.booking, this.booking = booking);
	}

	public void setBookingType(final BookingType bookingType)
	{
		this.propertyChangeSupport.firePropertyChange("bookingType", this.bookingType, this.bookingType = bookingType);
	}

	public void setCount(final int count)
	{
		this.propertyChangeSupport.firePropertyChange("count", this.count, this.count = count);
	}

	public void setDate(final Calendar date)
	{
		this.propertyChangeSupport.firePropertyChange("date", this.date, this.date = date);
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

	public void setFree(boolean free) 
	{
		this.propertyChangeSupport.firePropertyChange("free", this.free, this.free = free);
	}

	public boolean isFree() 
	{
		return free;
	}

	public static void copy(final Participant source, final Participant target)
	{
		target.setBooking(source.getBooking());
		target.setBookingType(source.getBookingType());
		target.setCount(source.getCount());
		target.setDate(source.getDate());
		target.setDeleted(source.isDeleted());
		target.setId(source.getId());
		target.setInserted(source.getInserted());
		target.setLink(source.getLink());
		target.setUpdated(source.getUpdated());
		target.setUser(source.getUser());
		target.setVersion(source.getVersion());
	}

	public static Participant newInstance()
	{
		return (Participant) AbstractEntity.newInstance(new Participant());
	}

	public static Participant newInstance(final Booking booking)
	{
		return (Participant) AbstractEntity.newInstance(new Participant(booking));
	}

	// public static long count(Person person)
	// {
	// Expression expression = new
	// ExpressionBuilder(Participant.class).get("person").equal(person);
	// Object object = AbstractEntity.server.count(Participant.class,
	// expression);
	// return 0l;
	// }

	// public static int count(LinkPersonAddress link)
	// {
	// ReportQuery query = new ReportQuery();
	// ExpressionBuilder builder = query.getExpressionBuilder();
	// query.setSelectionCriteria(builder.get("link").equal(link).and(
	// new ExpressionBuilder().get("deleted").equal(false)));
	// query.setShouldReturnSingleResult(true);
	// query.addCount();
	// Object object = Server.getInstance().getSession().executeQuery(query);
	// return 0;
	// }

	public static Participant newInstance(final LinkPersonAddress link, final Booking booking)
	{
		return (Participant) AbstractEntity.newInstance(new Participant(link, booking));
	}

}
