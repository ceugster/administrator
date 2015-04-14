package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.Participant;

public class BookingMap extends AbstractDataMap
{
	private static NumberFormat amountFormatter = null;

	private static DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	private static NumberFormat integerFormatter = DecimalFormat.getIntegerInstance();

	protected BookingMap() {
		super();
	}

	public BookingMap(final Booking booking)
	{
		this(booking, false);
	}

	public BookingMap(final Booking booking, final boolean loadTables)
	{
		if (amountFormatter == null)
		{
			amountFormatter = DecimalFormat.getNumberInstance();
			amountFormatter.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setGroupingUsed(true);
		}
		for (Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(booking));
		}
		this.setProperties(new ParticipantMap(booking.getParticipant()).getProperties());
		this.setProperties(new CourseMap(booking.getCourse()).getProperties());
		if (loadTables)
		{
			for (TableKey key : TableKey.values())
			{
				this.addTableMaps(key.getKey(), key.getTableMaps(booking));
			}
		}
//		this.addTableMaps(TableKey.PARTICIPANTS.getName(), TableKey.PARTICIPANTS.getTableMaps(booking));
//		this.addTableMaps(TableKey.COURSE_DETAILS.getName(), TableKey.COURSE_DETAILS.getTableMaps(booking));
	}

	protected void printReferences(Writer writer)
	{
		printHeader(writer, 2, "Referenzen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, "#participant", "Teilnehmer");
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, "#course", "Kurs");
		endTableRow(writer);
		endTable(writer);
	}

	protected void printTables(Writer writer)
	{
		printHeader(writer, 2, "Tabellen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, null, TableKey.PARTICIPANTS.getKey());
		printCell(writer, "#participant", "Teilnehmer");
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, null, TableKey.COURSE_DETAILS.getKey());
		printCell(writer, "#course_detail", "Kursdetails");
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, null, TableKey.COURSE_GUIDES.getKey());
		printCell(writer, "#course_guide", "Kursleitungen");
		endTableRow(writer);
		endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		AMOUNT_DETAILED, AMOUNT, ANNULATION_STATE, BOOKING_CONFIRMATION_SENT_DATE, BOOKING_STATE, BOOKING_DATE, DONE_STATE, FORTHCOMING_STATE, INVITATION_SENT_DATE, NOTE, PARTICIPANT_COUNT, PARTICIPATION_CONFIRMATION_SENT_DATE, PAYED_AMOUNT, PAYED_BACK_AMOUNT, PAYED_DATE, PAYED_BACK_DATE, STATE, PAYMENT_TERM;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case AMOUNT_DETAILED:
				{
					return "Buchungsbetrag detailliert";
				}
				case AMOUNT:
				{
					return "Buchungsbetrag";
				}
				case ANNULATION_STATE:
				{
					return "Annulationsstatus";
				}
				case BOOKING_CONFIRMATION_SENT_DATE:
				{
					return "Datum Buchungsbestätigung gesendet";
				}
				case BOOKING_STATE:
				{
					return "Buchungsstatus";
				}
				case BOOKING_DATE:
				{
					return "Buchungsdatum";
				}
				case DONE_STATE:
				{
					return "Durchführungsstatus";
				}
				case FORTHCOMING_STATE:
				{
					return "Anmeldestatus";
				}
				case INVITATION_SENT_DATE:
				{
					return "Datum Einladung verschickt";
				}
				case NOTE:
				{
					return "Bemerkungen";
				}
				case PARTICIPANT_COUNT:
				{
					return "Anzahl Teilnehmer";
				}
				case PARTICIPATION_CONFIRMATION_SENT_DATE:
				{
					return "Datum Teilnahmebestätigungen versendet";
				}
				case PAYED_AMOUNT:
				{
					return "Bezahlter Betrag";
				}
				case PAYED_BACK_AMOUNT:
				{
					return "Betrag Rückzahlung";
				}
				case PAYED_DATE:
				{
					return "Datum Zahlungseingang";
				}
				case PAYED_BACK_DATE:
				{
					return "Datum erfolgte Rückzahlung";
				}
				case STATE:
				{
					return "Status";
				}
				case PAYMENT_TERM:
				{
					return "Zahlungsbedingungen";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getKey()
		{
			switch (this)
			{
				case AMOUNT_DETAILED:
				{
					return "total_amount_detailed";
				}
				case AMOUNT:
				{
					return "total_amount";
				}
				case ANNULATION_STATE:
				{
					return "booking_annulation_state";
				}
				case BOOKING_CONFIRMATION_SENT_DATE:
				{
					return "booking_booking_confirmation_sent_date";
				}
				case BOOKING_STATE:
				{
					return "booking_booking_state";
				}
				case BOOKING_DATE:
				{
					return "booking_date";
				}
				case DONE_STATE:
				{
					return "booking_done_state";
				}
				case FORTHCOMING_STATE:
				{
					return "booking_forthcoming_state";
				}
				case INVITATION_SENT_DATE:
				{
					return "booking_invitation_sent_date";
				}
				case NOTE:
				{
					return "booking_note";
				}
				case PARTICIPANT_COUNT:
				{
					return "booking_participant_count";
				}
				case PARTICIPATION_CONFIRMATION_SENT_DATE:
				{
					return "booking_participation_sent_date";
				}
				case PAYED_AMOUNT:
				{
					return "booking_payed_amount";
				}
				case PAYED_BACK_AMOUNT:
				{
					return "booking_payed_back_amount";
				}
				case PAYED_DATE:
				{
					return "booking_payed_date";
				}
				case PAYED_BACK_DATE:
				{
					return "booking_payed_back_date";
				}
				case STATE:
				{
					return "booking_state";
				}
				case PAYMENT_TERM:
				{
					return "payment_term";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getName()
		{
			switch (this)
			{
				case AMOUNT_DETAILED:
				{
					return "Buchungsbetrag detailliert";
				}
				case AMOUNT:
				{
					return "Buchungsbetrag";
				}
				case ANNULATION_STATE:
				{
					return "Annulationsstatus";
				}
				case BOOKING_CONFIRMATION_SENT_DATE:
				{
					return "Buchungsbestätigung";
				}
				case BOOKING_STATE:
				{
					return "Buchungsstatus";
				}
				case BOOKING_DATE:
				{
					return "Buchungsdatum";
				}
				case DONE_STATE:
				{
					return "Durchführungsstatus";
				}
				case FORTHCOMING_STATE:
				{
					return "Anmeldestatus";
				}
				case INVITATION_SENT_DATE:
				{
					return "Einladung";
				}
				case NOTE:
				{
					return "Bemerkungen";
				}
				case PARTICIPANT_COUNT:
				{
					return "Teilnehmer";
				}
				case PARTICIPATION_CONFIRMATION_SENT_DATE:
				{
					return "Teilnahmebestätigungen";
				}
				case PAYED_AMOUNT:
				{
					return "Bezahlt";
				}
				case PAYED_BACK_AMOUNT:
				{
					return "Rückzahlung";
				}
				case PAYED_DATE:
				{
					return "Zahlungseingang";
				}
				case PAYED_BACK_DATE:
				{
					return "Rückzahlung";
				}
				case STATE:
				{
					return "Status";
				}
				case PAYMENT_TERM:
				{
					return "Zahlungsbedingungen";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Booking booking)
		{
			switch (this)
			{
				case AMOUNT_DETAILED:
				{
					String amount = amountFormatter.format(booking.getAmount());
					List<BookingType> bookingTypes = booking.getCourse().getBookingTypes();
					for (BookingType bookingType : bookingTypes)
					{
						int participantCount = booking.countParticipants(bookingType);
						if (participantCount > 0)
						{
							amount = amount + ", " + participantCount + " x " + bookingType.getName() + " à " + amountFormatter.format(bookingType.getPrice());
						}
					}
					return amount;
				}
				case AMOUNT:
				{
					return amountFormatter.format(booking.getAmount());
				}
				case ANNULATION_STATE:
				{
					IBookingState state = booking.getAnnulatedState();
					return state == null ? "" : state.toString();
				}
				case BOOKING_CONFIRMATION_SENT_DATE:
				{
					Calendar calendar = booking.getBookingConfirmationSentDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case BOOKING_STATE:
				{
					IBookingState state = booking.getBookingState(booking.getCourse().getState());
					return state == null ? "" : state.toString();
				}
				case BOOKING_DATE:
				{
					Calendar calendar = booking.getDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case DONE_STATE:
				{
					IBookingState state = booking.getDoneState();
					return state == null ? "" : state.toString();
				}
				case FORTHCOMING_STATE:
				{
					IBookingState state = booking.getForthcomingState();
					return state == null ? "" : state.toString();
				}
				case INVITATION_SENT_DATE:
				{
					Calendar calendar = booking.getInvitationSentDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case NOTE:
				{
					return booking.getNote() == null ? "" : booking.getNote();
				}
				case PARTICIPANT_COUNT:
				{
					return integerFormatter.format(booking.getParticipantCount());
				}
				case PARTICIPATION_CONFIRMATION_SENT_DATE:
				{
					Calendar calendar = booking.getParticipationConfirmationSentDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case PAYED_AMOUNT:
				{
					return amountFormatter.format(booking.getPayAmount());
				}
				case PAYED_BACK_AMOUNT:
				{
					return amountFormatter.format(booking.getPayBackAmount());
				}
				case PAYED_DATE:
				{
					Calendar calendar = booking.getPayDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case PAYED_BACK_DATE:
				{
					Calendar calendar = booking.getPayBackDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case STATE:
				{
					IBookingState state = booking.getState();
					return state == null ? "" : state.toString();
				}
				case PAYMENT_TERM:
				{
					return booking.getPaymentTerm() == null ? "" : booking.getPaymentTerm().getText();
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

	public enum TableKey implements DataMapKey
	{
		PARTICIPANTS, COURSE_DETAILS, COURSE_GUIDES;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case PARTICIPANTS:
				{
					return "Teilnehmer";
				}
				case COURSE_DETAILS:
				{
					return "Kursdaten";
				}
				case COURSE_GUIDES:
				{
					return "Kursleitung";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getKey()
		{
			switch (this)
			{
				case PARTICIPANTS:
				{
					return "table_participants";
				}
				case COURSE_DETAILS:
				{
					return "table_course_details";
				}
				case COURSE_GUIDES:
				{
					return "table_course_guides";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getName()
		{
			switch (this)
			{
				case PARTICIPANTS:
				{
					return "Teilnehmer";
				}
				case COURSE_DETAILS:
				{
					return "Kursdaten";
				}
				case COURSE_GUIDES:
				{
					return "Kursleitung";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap> getTableMaps(final Booking booking)
		{
			switch (this)
			{
				case PARTICIPANTS:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					List<Participant> participants = booking.getParticipants();
					for (Participant participant : participants)
					{
						tableMaps.add(new ParticipantMap(participant));
					}
					return tableMaps;
				}
				case COURSE_DETAILS:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					List<CourseDetail> courseDetails = booking.getCourse().getCourseDetails();
					for (CourseDetail courseDetail : courseDetails)
					{
						tableMaps.add(new CourseDetailMap(courseDetail));
					}
					return tableMaps;
				}
				case COURSE_GUIDES:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					List<CourseGuide> courseGuides = booking.getCourse().getCourseGuides();
					for (CourseGuide courseGuide : courseGuides)
					{
						tableMaps.add(new CourseGuideMap(courseGuide, false));
					}
					return tableMaps;
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
