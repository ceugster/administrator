package ch.eugster.events.persistence.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ch.eugster.events.persistence.formatters.CourseFormatter;

@Entity
@Table(name = "events_course")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "course_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "course_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "course_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "course_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "course_version")) })
public class Course extends AbstractEntity
{
	/*
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "course_responsible_user_id", referencedColumnName = "user_id")
	private User responsibleUser;

	@ManyToOne
	@JoinColumn(name = "course_domain_id", referencedColumnName = "domain_id")
	private Domain domain;

	@ManyToOne
	@JoinColumn(name = "course_category_id", referencedColumnName = "category_id")
	private Category category;

	@ManyToOne
	@JoinColumn(name = "course_rubric_id", referencedColumnName = "rubric_id")
	private Rubric rubric;

	@ManyToOne
	@JoinColumn(name = "course_season_id", referencedColumnName = "season_id")
	private Season season;

	/*
	 * --------------------------------------------------------------------------
	 * -------- Data Section
	 * ----------------------------------------------------
	 * ------------------------------
	 */
	@Id
	@Column(name = "course_id")
	@GeneratedValue(generator = "events_course_id_seq")
	@TableGenerator(name = "events_course_id_seq", table = "events_sequence", initialValue = 1000, allocationSize = 5)
	private Long id;

	/*
	 * Kurscode
	 */
	@Basic
	@Column(name = "course_code")
	private String code;

	/*
	 * Kursbezeichnung
	 */
	@Basic
	@Column(name = "course_title")
	private String title;

	/*
	 * Kursteaser
	 */
	@Basic
	@Column(name = "course_teaser")
	private String teaser;

	/*
	 * Kursbeschreibung
	 */
	@Basic
	@Column(name = "course_desc")
	private String description;

	/*
	 * --------------------------------------------------------------------------
	 * -------- Content Section
	 * --------------------------------------------------
	 * --------------------------------
	 */
	/*
	 * Verpflegung
	 */
	@Basic
	@Column(name = "course_boarding")
	private String boarding;

	/*
	 * Unterkunft
	 */
	@Basic
	@Column(name = "course_lodging")
	private String lodging;

	/*
	 * Kurszweck
	 */
	@Basic
	@Column(name = "course_purpose")
	private String purpose;

	/*
	 * Kursinhalt
	 */
	@Basic
	@Column(name = "course_contents")
	private String contents;

	/*
	 * Kursmaterial
	 */
	@Basic
	@Column(name = "course_material_participants")
	private String materialParticipants;

	@Basic
	@Column(name = "course_material_organizer")
	private String materialOrganizer;

	/*
	 * --------------------------------------------------------------------------
	 * -------- Bedingungen Section
	 * ----------------------------------------------
	 * ------------------------------------
	 */
	/*
	 * Zielpublikum
	 */
	@Basic
	@Column(name = "course_target_public")
	private String targetPublic;

	/*
	 * Voraussetzungen
	 */
	@Basic
	@Column(name = "course_prerequisites")
	private String prerequisites;

	/*
	 * Mindestanzahl Teilnehmer (Buchungen können mehrere Teilnehmer enthalten)
	 */
	@Basic
	@Column(name = "course_min_participants")
	private int minParticipants;

	/*
	 * Maximale Anzahl Teilnehmer
	 */
	@Basic
	@Column(name = "course_max_participants")
	private int maxParticipants;

	/*
	 * Mindestalter
	 */
	@Basic
	@Column(name = "course_min_age")
	private int minAge;

	/*
	 * Maximales Alter
	 */
	@Basic
	@Column(name = "course_max_age")
	private int maxAge;

	/*
	 * Einschränkung des Geschlechts
	 */
	@Basic
	@Column(name = "course_sex")
	private CourseSexConstraint sex;

	/*
	 * Zahlungsbedingungen
	 */
	@ManyToOne
	@JoinColumn(name = "course_payment_term_id", referencedColumnName = "payment_term_id")
	private PaymentTerm paymentTerm;

	/*
	 * --------------------------------------------------------------------------
	 * -------- Information Section
	 * ----------------------------------------------
	 * ------------------------------------
	 */
	/*
	 * Vorinformatives Treffen
	 */
	@Basic
	@Column(name = "course_info_meeting")
	private String infoMeeting;

	/*
	 * Auskunft
	 */
	@Basic
	@Column(name = "course_information")
	private String information;

	/*
	 * Bemerkungen Kurskosten 
	 */
	@Basic
	@Column(name = "course_cost_note")
	private String costNote;
	/*
	 * --------------------------------------------------------------------------
	 * -------- Durchführungsbedingungen
	 * ----------------------------------------
	 * ------------------------------------------
	 */
	@Basic
	@Column(name = "course_realization")
	private String realization;

	/*
	 * --------------------------------------------------------------------------
	 * -------- Termine Section
	 * --------------------------------------------------
	 * --------------------------------
	 */
	/*
	 * Anmeldungsfrist
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_last_booking_date")
	private Calendar lastBookingDate;

	/*
	 * Versand Voranzeige
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_advance_notice_date")
	private Calendar advanceNoticeDate;

	/*
	 * Voranzeige verschickt am
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_advance_notice_done_date")
	private Calendar advanceNoticeDoneDate;

	/*
	 * Versand Einladung
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_invitation_date")
	private Calendar invitationDate;

	/*
	 * Einladung verschickt am
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_invitation_done_date")
	private Calendar invitationDoneDate;

	/*
	 * Letzter Termin für Annulation durch Kunden
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_last_annulation_date")
	private Calendar lastAnnulationDate;

	/*
	 * Termin, an dem die Annulation durch den Organisator durchgeführt wurde
	 */
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "course_annulation_date")
	private Calendar annulationDate;

	/*
	 * Status
	 */
	@Basic
	@Column(name = "course_state")
	private CourseState state;

	/*
	 * Verschiebestatus
	 */
	@Basic
	@Column(name = "course_substituted")
	private boolean substituted = false;

	/*
	 * Children
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
	private List<BookingType> bookingTypes = new Vector<BookingType>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
	private final List<Booking> bookings = new Vector<Booking>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
	private List<CourseDetail> courseDetails = new Vector<CourseDetail>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
	private List<CourseGuide> courseGuides = new Vector<CourseGuide>();

	/*
	 * Constructor
	 */
	private Course()
	{
		super();
	}

	/*
	 * Constructor
	 */
	private Course(final Season season)
	{
		super();
		this.setSeason(season);
	}

	public void addBooking(final Booking booking)
	{
		this.propertyChangeSupport.firePropertyChange("bookings", this.bookings, this.bookings.add(booking));
	}

	public void addBookingType(final BookingType bookingType)
	{
		this.propertyChangeSupport.firePropertyChange("bookingTypes", this.bookingTypes,
				this.bookingTypes.add(bookingType));
	}

	public void addCourseDetail(final CourseDetail courseDetail)
	{
		this.propertyChangeSupport.firePropertyChange("courseDetails", this.courseDetails,
				this.courseDetails.add(courseDetail));
	}

	public void addCourseGuide(final CourseGuide courseGuide)
	{
		this.propertyChangeSupport.firePropertyChange("courseGuides", this.courseGuides,
				this.courseGuides.add(courseGuide));
	}

	public Course copy(Season season)
	{
		Course copy = Course.newInstance(season);
		copy.setBoarding(this.getBoarding());
		copy.setCategory(this.getCategory());
		copy.setContents(this.getContents());
		copy.setDescription(this.getDescription());
		copy.setDomain(this.getDomain());
		copy.setInfoMeeting(this.getInfoMeeting());
		copy.setInformation(this.getInformation());
		copy.setInvitationDate(this.getInvitationDate());
		copy.setInvitationDoneDate(this.getInvitationDoneDate());
		copy.setLastAnnulationDate(this.getLastAnnulationDate());
		copy.setLastBookingDate(this.getLastBookingDate());
		copy.setLodging(this.getLodging());
		copy.setMaterialOrganizer(this.getMaterialOrganizer());
		copy.setMaterialParticipants(this.getMaterialParticipants());
		copy.setMaxAge(this.getMaxAge());
		copy.setMaxParticipants(this.getMaxParticipants());
		copy.setMinAge(this.getMinAge());
		copy.setMinParticipants(this.getMinParticipants());
		copy.setPurpose(this.getPurpose());
		copy.setRealization(this.getRealization());
		copy.setRubric(this.getRubric());
		copy.setSex(this.getSex());
		copy.setState(CourseState.FORTHCOMING);
		copy.setSubstituted(this.isSubstituted());
		copy.setTargetPublic(this.getTargetPublic());
		copy.setTeaser(this.getTeaser());
		copy.setTitle(this.getTitle());
		copy.setResponsibleUser(this.getResponsibleUser());
		return copy;
	}

	public Calendar getAnnulationDate()
	{
		return this.annulationDate;
	}

	public String getBoarding()
	{
		return AbstractEntity.stringValueOf(this.boarding);
	}

	public Booking getBooking(final Person person)
	{
		for (Booking booking : this.getBookings())
		{
			if (!booking.isDeleted() && booking.hasParticipant(person))
			{
				return booking;
			}
		}
		return null;
	}

	public List<Booking> getBookings()
	{
		List<Booking> bookings = new Vector<Booking>();
		for (Booking booking : this.bookings)
		{
			if (!booking.isDeleted())
			{
				bookings.add(booking);
			}
		}
		return bookings;
	}

	public List<Booking> getBookings(IBookingState bookingState)
	{
		List<Booking> bookings = new Vector<Booking>();
		for (Booking booking : this.bookings)
		{
			if (!booking.isDeleted())
			{
				if (booking.getBookingState(booking.getCourse().getState()).equals(bookingState))
				{
					bookings.add(booking);
				}
			}
		}
		return bookings;
	}

	public IBookingState[] getBookingStates(final CourseState courseState)
	{
		IBookingState[] states = null;

		if (courseState.equals(CourseState.FORTHCOMING))
		{
			states = BookingForthcomingState.values();
			for (IBookingState state : states)
				state.setCount(0);

			for (Booking booking : this.getBookings())
			{
				if (!booking.isDeleted())
				{
					states[booking.getForthcomingState().ordinal()].add(booking.getParticipantCount());
				}
			}
		}
		else if (courseState.equals(CourseState.DONE))
		{
			states = BookingDoneState.values();
			for (IBookingState state : states)
				state.setCount(0);

			for (Booking booking : this.getBookings())
			{
				if (!booking.isDeleted())
				{
					states[booking.getDoneState().ordinal()].add(booking.getParticipantCount());
				}
			}
		}
		else if (courseState.equals(CourseState.ANNULATED))
		{
			states = BookingAnnulatedState.values();
			for (IBookingState state : states)
				state.setCount(0);

			for (Booking booking : this.getBookings())
			{
				if (!booking.isDeleted())
				{
					states[booking.getAnnulatedState().ordinal()].add(booking.getParticipantCount());
				}
			}
		}

		return states;
	}

	public List<BookingType> getBookingTypes()
	{
		List<BookingType> types = new ArrayList<BookingType>();
		for (BookingType type : this.bookingTypes)
		{
			if (!type.isDeleted())
			{
				types.add(type);
			}
		}
		return types;
	}

	public Category getCategory()
	{
		return this.category;
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getContents()
	{
		return AbstractEntity.stringValueOf(this.contents);
	}

	public List<CourseDetail> getCourseDetails()
	{
		List<CourseDetail> details = new ArrayList<CourseDetail>();
		for (CourseDetail detail : this.courseDetails)
		{
			if (!detail.isDeleted())
			{
				details.add(detail);
			}
		}
		return details;
	}

	public List<CourseGuide> getCourseGuides()
	{
		List<CourseGuide> guides = new ArrayList<CourseGuide>();
		for (CourseGuide guide : this.courseGuides)
		{
			if (!guide.isDeleted() && !guide.getGuide().isDeleted())
			{
				guides.add(guide);
			}
		}
		return guides;
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	public Domain getDomain()
	{
		return this.domain;
	}

	public Calendar getFirstDate()
	{
		Calendar startDate = null;
		for (CourseDetail courseDetail : this.courseDetails)
		{
			if (!courseDetail.isDeleted() && courseDetail.getStart() != null)
			{
				if (startDate == null)
				{
					startDate = courseDetail.getStart();
				}
				else
				{
					if (courseDetail.getStart().getTimeInMillis() < startDate.getTimeInMillis())
						startDate = courseDetail.getStart();
				}
			}
		}
		return startDate;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getInfoMeeting()
	{
		return AbstractEntity.stringValueOf(this.infoMeeting);
	}

	public String getInformation()
	{
		return AbstractEntity.stringValueOf(this.information);
	}

	public Calendar getInvitationDate()
	{
		return this.invitationDate;
	}

	public Calendar getInvitationDoneDate()
	{
		return this.invitationDoneDate;
	}

	public Calendar getLastAnnulationDate()
	{
		return this.lastAnnulationDate;
	}

	public Calendar getLastBookingDate()
	{
		return this.lastBookingDate;
	}

	public Calendar getLastDate()
	{
		Calendar endDate = null;
		for (CourseDetail courseDetail : this.courseDetails)
		{
			if (!courseDetail.isDeleted() && courseDetail.getEnd() != null)
			{
				if (endDate == null)
				{
					endDate = courseDetail.getEnd();
				}
				else
				{
					if (endDate.getTimeInMillis() < courseDetail.getEnd().getTimeInMillis())
						endDate = courseDetail.getEnd();
				}
			}
		}
		return endDate;
	}

	public String getLodging()
	{
		return AbstractEntity.stringValueOf(this.lodging);
	}

	public String getMaterialOrganizer()
	{
		return AbstractEntity.stringValueOf(this.materialOrganizer);
	}

	public String getMaterialParticipants()
	{
		return AbstractEntity.stringValueOf(this.materialParticipants);
	}

	public int getMaxAge()
	{
		return this.maxAge;
	}

	public int getMaxParticipants()
	{
		return this.maxParticipants;
	}

	public int getMinAge()
	{
		return this.minAge;
	}

	public int getMinParticipants()
	{
		return this.minParticipants;
	}

	public int getParticipantsCount()
	{
		int count = 0;

		for (Booking booking : this.getBookings())
		{
			if (!booking.isDeleted())
			{
				count += booking.getParticipantCount();
			}
		}
		return count;
	}

	public int getBookedParticipantsCount()
	{
		int count = 0;

		for (Booking booking : this.getBookings())
		{
			if (!booking.isDeleted())
			{
				if (booking.getBookingState(booking.getCourse().getState()).equals(BookingForthcomingState.BOOKED))
				{
					count += booking.getParticipantCount();
				}
			}
		}
		return count;
	}

	public int getBookedParticipantsCount(boolean saved)
	{
		int count = 0;

		for (Booking booking : this.getBookings())
		{
			if (!booking.isDeleted())
			{
				if (booking.getBookingState(booking.getCourse().getState()).equals(BookingForthcomingState.BOOKED)
						|| booking.getBookingState(booking.getCourse().getState()).equals(
								BookingForthcomingState.PROVISIONAL_BOOKED))
				{
					count += booking.getBookedParticipantCount(saved);
				}
			}
		}
		return count;
	}

	public int getParticipantsCount(final IBookingState state)
	{
		int count = 0;
		for (Booking booking : this.getBookings())
		{
			if (!booking.isDeleted())
			{
				if (booking.getState().equals(state))
				{
					count += booking.getParticipantCount();
				}
			}
		}
		return count;
	}

	public String getPurpose()
	{
		return AbstractEntity.stringValueOf(this.purpose);
	}

	public String getRealization()
	{
		return AbstractEntity.stringValueOf(this.realization);
	}

	public User getResponsibleUser()
	{
		return this.responsibleUser;
	}

	public Rubric getRubric()
	{
		return this.rubric;
	}

	public Season getSeason()
	{
		return this.season;
	}

	public CourseSexConstraint getSex()
	{
		return this.sex == null ? CourseSexConstraint.BOTH : this.sex;
	}

	public CourseState getState()
	{
		return this.state == null ? CourseState.FORTHCOMING : this.state;
	}

	public String getTargetPublic()
	{
		return AbstractEntity.stringValueOf(this.targetPublic);
	}

	public String getTeaser()
	{
		return AbstractEntity.stringValueOf(this.teaser);
	}

	public String getTitle()
	{
		return AbstractEntity.stringValueOf(this.title);
	}

	public boolean hasParticipant(final Person person)
	{
		for (Booking booking : this.getBookings())
		{
			if (booking.hasParticipant(person))
				return true;
		}
		return false;
	}

	public boolean isSubstituted()
	{
		return this.substituted;
	}

	public void removeBooking(final Booking booking)
	{
		this.propertyChangeSupport.firePropertyChange("bookings", this.bookings, this.bookings.remove(booking));
	}

	public void removeBookingType(final BookingType bookingType)
	{
		this.propertyChangeSupport.firePropertyChange("bookingTypes", this.bookingTypes,
				this.bookingTypes.remove(bookingType));
	}

	public void removeCourseDetail(final CourseDetail courseDetail)
	{
		this.propertyChangeSupport.firePropertyChange("courseDetails", this.courseDetails,
				this.courseDetails.remove(courseDetail));
	}

	public void removeCourseGuide(final CourseGuide courseGuide)
	{
		this.propertyChangeSupport.firePropertyChange("courseGuides", this.courseGuides,
				this.courseGuides.remove(courseGuide));
	}

	public void setAnnulationDate(final Calendar annulationDate)
	{
		this.propertyChangeSupport.firePropertyChange("annulationDate", this.annulationDate,
				this.annulationDate = annulationDate);
		if (this.annulationDate == null)
		{
			Calendar courseDate = null;
			List<CourseDetail> details = this.getCourseDetails();
			for (CourseDetail detail : details)
			{
				if (this.isSubstituted())
				{
					if (detail.isWithSubstituteDate() && detail.getSubstituteEnd() != null)
					{
						if (courseDate == null
								|| courseDate.getTimeInMillis() < detail.getSubstituteEnd().getTimeInMillis())
						{
							courseDate = detail.getSubstituteEnd();
						}
					}
					else if (detail.getEnd() != null)
					{
						if (courseDate == null || courseDate.getTimeInMillis() < detail.getEnd().getTimeInMillis())
						{
							courseDate = detail.getEnd();
						}
					}
				}
				else
				{
					if (detail.getEnd() != null)
					{
						if (courseDate == null || courseDate.getTimeInMillis() < detail.getEnd().getTimeInMillis())
						{
							courseDate = detail.getEnd();
						}
					}
				}
			}
			if (courseDate == null)
			{
				this.state = CourseState.FORTHCOMING;
			}
			else if (courseDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
			{
				this.state = CourseState.DONE;
			}
			else
			{
				this.state = CourseState.FORTHCOMING;
			}
		}
		else
			this.state = CourseState.ANNULATED;
	}

	public void setBoarding(final String boarding)
	{
		this.propertyChangeSupport.firePropertyChange("boarding", this.boarding, this.boarding = boarding);
	}

	public void setCategory(final Category category)
	{
		this.propertyChangeSupport.firePropertyChange("category", this.category, this.category = category);
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	public void setContents(final String contents)
	{
		this.propertyChangeSupport.firePropertyChange("contents", this.contents, this.contents = contents);
	}

	@Override
	public void setDeleted(final boolean deleted)
	{
		super.setDeleted(deleted);

		for (Booking booking : this.bookings)
		{
			if (!booking.isDeleted())
				booking.setDeleted(deleted);
		}

		for (BookingType bookingType : this.bookingTypes)
		{
			if (!bookingType.isDeleted())
				bookingType.setDeleted(deleted);
		}

		for (CourseGuide courseGuide : this.courseGuides)
		{
			if (!courseGuide.isDeleted())
				courseGuide.setDeleted(deleted);
		}

		for (CourseDetail courseDetail : this.courseDetails)
		{
			if (!courseDetail.isDeleted())
				courseDetail.setDeleted(deleted);
		}

	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
	}

	public void setDomain(final Domain domain)
	{
		this.propertyChangeSupport.firePropertyChange("domain", this.domain, this.domain = domain);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setInfoMeeting(final String infoMeeting)
	{
		this.propertyChangeSupport.firePropertyChange("infoMeeting", this.infoMeeting, this.infoMeeting = infoMeeting);
	}

	public void setInformation(final String information)
	{
		this.propertyChangeSupport.firePropertyChange("information", this.information, this.information = information);
	}

	public void setInvitationDate(final Calendar invitationDate)
	{
		this.propertyChangeSupport.firePropertyChange("invitationDate", this.invitationDate,
				this.invitationDate = invitationDate);
	}

	public void setInvitationDoneDate(final Calendar invitationDoneDate)
	{
		this.propertyChangeSupport.firePropertyChange("invitationDoneDate", this.invitationDoneDate,
				this.invitationDoneDate = invitationDoneDate);
	}

	public void setLastAnnulationDate(final Calendar lastAnnulationDate)
	{
		this.propertyChangeSupport.firePropertyChange("lastAnnulationDate", this.lastAnnulationDate,
				this.lastAnnulationDate = lastAnnulationDate);
		this.lastAnnulationDate = lastAnnulationDate;
	}

	public void setLastBookingDate(final Calendar lastBookingDate)
	{
		this.propertyChangeSupport.firePropertyChange("lastBookingDate", this.lastBookingDate,
				this.lastBookingDate = lastBookingDate);
	}

	public void setLodging(final String lodging)
	{
		this.propertyChangeSupport.firePropertyChange("lodging", this.lodging, this.lodging = lodging);
	}

	public void setMaterialOrganizer(final String materialOrganizer)
	{
		this.propertyChangeSupport.firePropertyChange("materialOrganizer", this.materialOrganizer,
				this.materialOrganizer = materialOrganizer);
	}

	public void setMaterialParticipants(final String materialParticipants)
	{
		this.propertyChangeSupport.firePropertyChange("materialParticipants", this.materialParticipants,
				this.materialParticipants = materialParticipants);
	}

	public void setMaxAge(final int maxAge)
	{
		this.propertyChangeSupport.firePropertyChange("maxAge", this.maxAge, this.maxAge = maxAge);
	}

	public void setMaxParticipants(final int maxParticipants)
	{
		this.propertyChangeSupport.firePropertyChange("maxParticipants", this.maxParticipants,
				this.maxParticipants = maxParticipants);
	}

	public void setMinAge(final int minAge)
	{
		this.propertyChangeSupport.firePropertyChange("minAge", this.minAge, this.minAge = minAge);
	}

	public void setMinParticipants(final int minParticipants)
	{
		this.propertyChangeSupport.firePropertyChange("minParticipants", this.minParticipants,
				this.minParticipants = minParticipants);
	}

	public void setPurpose(final String purpose)
	{
		this.propertyChangeSupport.firePropertyChange("purpose", this.purpose, this.purpose = purpose);
	}

	public void setRealization(final String realization)
	{
		this.propertyChangeSupport.firePropertyChange("realization", this.realization, this.realization = realization);
	}

	public void setResponsibleUser(final User responsibleUser)
	{
		this.propertyChangeSupport.firePropertyChange("responsibleUser", this.responsibleUser,
				this.responsibleUser = responsibleUser);
	}

	public void setRubric(final Rubric rubric)
	{
		this.propertyChangeSupport.firePropertyChange("rubric", this.rubric, this.rubric = rubric);
	}

	public void setSeason(final Season season)
	{
		this.propertyChangeSupport.firePropertyChange("season", this.season, this.season = season);
	}

	public void setSex(final CourseSexConstraint sex)
	{
		this.propertyChangeSupport.firePropertyChange("sex", this.sex, this.sex = sex);

	}

	public void setState(final CourseState state)
	{
		this.propertyChangeSupport.firePropertyChange("state", this.state, this.state = state);
	}

	public void setSubstituted(final boolean substituted)
	{
		this.propertyChangeSupport.firePropertyChange("substituted", this.substituted, this.substituted = substituted);
	}

	public void setTargetPublic(final String targetPublic)
	{
		this.propertyChangeSupport.firePropertyChange("targetPublic", this.targetPublic,
				this.targetPublic = targetPublic);
	}

	public void setTeaser(final String teaser)
	{
		this.propertyChangeSupport.firePropertyChange("teaser", this.teaser, this.teaser = teaser);
	}

	public void setTitle(final String title)
	{
		this.propertyChangeSupport.firePropertyChange("title", this.title, this.title = title);
	}

	public void setPaymentTerm(PaymentTerm paymentTerm)
	{
		this.propertyChangeSupport.firePropertyChange("paymentTerm", this.paymentTerm, this.paymentTerm = paymentTerm);
	}

	public PaymentTerm getPaymentTerm()
	{
		return paymentTerm;
	}

	public void setPrerequisites(String prerequisites) 
	{
		this.propertyChangeSupport.firePropertyChange("prerequisites", this.prerequisites, this.prerequisites = prerequisites);
	}

	public String getPrerequisites() 
	{
		return AbstractEntity.stringValueOf(prerequisites);
	}

	public void setCostNote(String costNote) 
	{
		this.propertyChangeSupport.firePropertyChange("costNote", this.costNote, this.costNote = costNote);
	}

	public String getCostNote() 
	{
		return stringValueOf(costNote);
	}

	public void setAdvanceNoticeDate(Calendar advanceNoticeDate)
	{
		this.propertyChangeSupport.firePropertyChange("advanceNoticeDate", this.advanceNoticeDate, this.advanceNoticeDate = advanceNoticeDate);
	}

	public Calendar getAdvanceNoticeDate() 
	{
		return advanceNoticeDate;
	}

	public void setAdvanceNoticeDoneDate(Calendar advanceNoticeDoneDate) 
	{
		this.propertyChangeSupport.firePropertyChange("advanceNoticeDoneDate", this.advanceNoticeDoneDate, this.advanceNoticeDoneDate = advanceNoticeDoneDate);
	}

	public Calendar getAdvanceNoticeDoneDate() 
	{
		return advanceNoticeDoneDate;
	}
	
	public String getEntityName()
	{
		return "Kurs";
	}

	public String getInstanceName()
	{
		return CourseFormatter.getInstance().formatComboEntry(this);
	}
	
	public static Course newInstance(final Season season)
	{
		return (Course) AbstractEntity.newInstance(new Course(season));
	}

}
