package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Customizer;

@Entity
@Table(name = "events_booking")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "booking_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "booking_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "booking_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "booking_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "booking_version")) })
@Customizer(DeletedFilter.class)
public class Booking extends AbstractEntity
{
	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "booking_course_id", referencedColumnName = "course_id")
	private Course course;

	/**
	 * participant ist der Teilnehmer einer Buchung, an den die Korrespondenzen
	 * und Rechnungen geschickt werden. Er muss immer vorhanden sein.
	 * 
	 * booking_participant
	 */
	@OneToOne
	@JoinColumn(name = "booking_participant_id", referencedColumnName = "participant_id")
	private Participant participant;

	/**
	 * Data
	 */
	@Id
	@Column(name = "booking_id")
	@GeneratedValue(generator = "events_booking_id_seq")
	@TableGenerator(name = "events_booking_id_seq", table = "events_sequence", initialValue = 20000, allocationSize = 5)
	private Long id;

	/*
	 * Datum der Buchung
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "booking_date")
	private Calendar date;

	/*
	 * Datum des Versands der Bestätigung für den aktuellen Status
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "booking_booking_confirmation_sent_date")
	private Calendar bookingConfirmationSentDate;

	/*
	 * Datum des Versands der Bestätigung für den aktuellen Status
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "booking_invitation_sent_date")
	private Calendar invitationSentDate;

	/*
	 * Datum des Versands der Bestätigung für den aktuellen Status
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "booking_participation_confirmation_sent_date")
	private Calendar participationConfirmationSentDate;

	/*
	 * Buchungsstatus Bevorstehender Kurs
	 */
	@Basic
	@Column(name = "booking_forthcoming_state")
	@Enumerated
	private BookingForthcomingState forthcomingState;

	/*
	 * Buchungsstatus Durchgeführter Kurs
	 */
	@Basic
	@Column(name = "booking_done_state")
	@Enumerated
	private BookingDoneState doneState;

	/*
	 * Buchungsstatus Bevorstehender Kurs
	 */
	@Basic
	@Column(name = "booking_annulated_state")
	@Enumerated
	private BookingAnnulatedState annulatedState;

	/*
	 * Datum des Zahlungseingangs
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "booking_pay_date")
	private Calendar payDate;

	/*
	 * Zahlungsbetrag
	 */
	@Basic
	@Column(name = "booking_pay_amount")
	private double payAmount;

	/*
	 * Datum der Rückzahlung (im Falle von Annulationen)
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "booking_pay_back_date")
	private Calendar payBackDate;

	/*
	 * Rückzahlungsbetrag (muss nicht dem Zahlungsbetrag entsprechen, wenn z.B.
	 * nur ein Teil zurückbezahlt wird.
	 */
	@Basic
	@Column(name = "booking_pay_back_amount")
	private double payBackAmount;

	/*
	 * Freies Bemerkungsfeld
	 */
	@Basic
	@Column(name = "booking_note")
	private String note;

	/*
	 * Teilnehmer in dieser Buchung
	 */
	@OneToMany(cascade = ALL, mappedBy = "booking")
	private Collection<Participant> participants = new Vector<Participant>();

	private Booking()
	{
		super();
	}

	private Booking(final Course course)
	{
		super();
		this.setCourse(course);
	}

	public void addParticipant(final Participant participant)
	{
		this.propertyChangeSupport.firePropertyChange("participants", this.participants,
				this.participants.add(participant));
	}

	/**
	 * 
	 * @return amount
	 */
	public double getAmount()
	{
		double amount = 0d;
		for (Participant participant : this.participants)
		{
			if (!participant.isDeleted() && participant.getBookingType() != null)
				amount += participant.getBookingType().getPrice();
		}
		return amount;
	}

	public BookingAnnulatedState getAnnulatedState()
	{
		return this.annulatedState == null ? BookingAnnulatedState.ANNULATED : this.annulatedState;
	}

	public Calendar getBookingConfirmationSentDate()
	{
		return this.bookingConfirmationSentDate;
	}

	public IBookingState getBookingState(final CourseState courseState)
	{
		if (courseState.equals(CourseState.FORTHCOMING))
		{
			return this.getForthcomingState();
		}
		else if (courseState.equals(CourseState.DONE))
		{
			return this.getDoneState();
		}
		else if (courseState.equals(CourseState.ANNULATED))
		{
			return this.getAnnulatedState();
		}

		return null;
	}

	public Course getCourse()
	{
		return this.course;
	}

	public Calendar getDate()
	{
		return this.date;
	}

	public BookingDoneState getDoneState()
	{
		return this.doneState == null ? BookingDoneState.PARTICIPATED : this.doneState;
	}

	public BookingForthcomingState getForthcomingState()
	{
		return this.forthcomingState == null ? BookingForthcomingState.BOOKED : this.forthcomingState;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public Calendar getInvitationSentDate()
	{
		return this.invitationSentDate;
	}

	public String getNote()
	{
		return AbstractEntity.stringValueOf(this.note);
	}

	public Participant getParticipant()
	{
		return this.participant;
	}

	public int getParticipantCount()
	{
		int count = 0;
		for (Participant participant : this.participants)
			if (!participant.isDeleted())
				count += participant.getCount();
		return count;
	}

	public Collection<Participant> getParticipants()
	{
		return this.participants;
	}

	public Calendar getParticipationConfirmationSentDate()
	{
		return this.participationConfirmationSentDate;
	}

	public double getPayAmount()
	{
		return this.payAmount;
	}

	public double getPayBackAmount()
	{
		return this.payBackAmount;
	}

	public Calendar getPayBackDate()
	{
		return this.payBackDate;
	}

	public Calendar getPayDate()
	{
		return this.payDate;
	}

	public IBookingState getState()
	{
		if (this.course == null)
			return null;

		if (this.course.getState().equals(CourseState.FORTHCOMING))
			return this.getForthcomingState();
		else if (this.course.getState().equals(CourseState.DONE))
			return this.getDoneState();
		else if (this.course.getState().equals(CourseState.ANNULATED))
			return this.getAnnulatedState();
		else
			throw new RuntimeException("Ungültiger Kursstatus");
	}

	public boolean hasCorrespondent()
	{
		return this.participant != null && !this.participant.isDeleted();
	}

	public boolean hasParticipant(final Person person)
	{
		for (Participant participant : this.participants)
		{
			if (!participant.isDeleted())
				if (participant.getLink().getPerson().equals(person))
					return true;
		}
		return false;
	}

	@PrePersist
	public void prePersist()
	{
		this.getCourse().addBooking(this);
	}

	public void removeParticipant(final Participant participant)
	{
		this.propertyChangeSupport.firePropertyChange("participants", this.participants,
				this.participants.remove(participant));
	}

	public void replaceParticipants(final Participant[] participantsToReplace)
	{
		for (Participant participant : participantsToReplace)
		{
			if (this.participants.contains(participant))
			{
				this.removeParticipant(participant);
				this.addParticipant(participant);
			}
		}
	}

	public void setAnnulatedState(final BookingAnnulatedState annulatedState)
	{
		this.propertyChangeSupport.firePropertyChange("annulatedState", this.annulatedState,
				this.annulatedState = annulatedState);
	}

	public void setBookingConfirmationSentDate(final Calendar bookingConfirmationSentDate)
	{
		this.propertyChangeSupport.firePropertyChange("bookingConfirmationSentDate", this.bookingConfirmationSentDate,
				this.bookingConfirmationSentDate = bookingConfirmationSentDate);
	}

	public void setCourse(final Course course)
	{
		this.propertyChangeSupport.firePropertyChange("course", this.course, this.course = course);
	}

	public void setDate(final Calendar date)
	{
		this.propertyChangeSupport.firePropertyChange("date", this.date, this.date = date);
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		super.setDeleted(deleted);
		for (Participant participant : this.participants)
			if (!participant.isDeleted())
				participant.setDeleted(deleted);
	}

	public void setDoneState(final BookingDoneState doneState)
	{
		this.propertyChangeSupport.firePropertyChange("doneState", this.doneState, this.doneState = doneState);
	}

	public void setForthcomingState(final BookingForthcomingState forthcomingState)
	{
		this.propertyChangeSupport.firePropertyChange("forthcomingState", this.forthcomingState,
				this.forthcomingState = forthcomingState);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setInvitationSentDate(final Calendar invitationSentDate)
	{
		this.propertyChangeSupport.firePropertyChange("invitationSentDate", this.invitationSentDate,
				this.invitationSentDate = invitationSentDate);
	}

	public void setNote(final String note)
	{
		this.propertyChangeSupport.firePropertyChange("note", this.note, this.note = note);
	}

	public void setParticipant(final Participant participant)
	{
		this.propertyChangeSupport.firePropertyChange("participant", this.participant, this.participant = participant);
	}

	public void setParticipants(final Collection<Participant> participants)
	{
		this.propertyChangeSupport.firePropertyChange("participants", this.participants,
				this.participants = participants);
	}

	public void setParticipationConfirmationSentDate(final Calendar participationConfirmationSentDate)
	{
		this.propertyChangeSupport.firePropertyChange("participationConfirmationSentDate",
				this.participationConfirmationSentDate,
				this.participationConfirmationSentDate = participationConfirmationSentDate);
	}

	public void setPayAmount(final double payAmount)
	{
		this.propertyChangeSupport.firePropertyChange("payAmount", this.payAmount, this.payAmount = payAmount);
	}

	public void setPayBackAmount(final double payBackAmount)
	{
		this.propertyChangeSupport.firePropertyChange("payBackAmount", this.payBackAmount,
				this.payBackAmount = payBackAmount);
	}

	public void setPayBackDate(final Calendar payBackDate)
	{
		this.propertyChangeSupport.firePropertyChange("payBackDate", this.payBackDate, this.payBackDate = payBackDate);
	}

	public void setPayDate(final Calendar payDate)
	{
		this.propertyChangeSupport.firePropertyChange("payDate", this.payDate, this.payDate = payDate);
	}

	public static Booking newInstance()
	{
		return (Booking) AbstractEntity.newInstance(new Booking());
	}

	public static Booking newInstance(final Course course)
	{
		return (Booking) AbstractEntity.newInstance(new Booking(course));
	}

}
