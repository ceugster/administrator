package ch.eugster.events.documents.maps;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.User;

public class CourseMap extends AbstractDataMap
{
	private static DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	private static NumberFormat integerFormatter = DecimalFormat.getIntegerInstance();

	public CourseMap(final Course course)
	{
		this(course, false);
	}

	public CourseMap(final Course course, final boolean loadTables)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(course));
		}
		if (course.getCategory() != null)
		{
			this.setProperties(new CategoryMap(course.getCategory()).getProperties());
		}
		if (course.getDomain() != null)
		{
			this.setProperties(new DomainMap(course.getDomain()).getProperties());
		}
		if (course.getRubric() != null)
		{
			this.setProperties(new RubricMap(course.getRubric()).getProperties());
		}
		if (course.getSeason() != null)
		{
			this.setProperties(new SeasonMap(course.getSeason()).getProperties());
		}
		if (loadTables)
		{
			for (TableKey key : TableKey.values())
			{
				this.addTableMaps(key.getKey(), key.getTableMaps(course));
			}
		}

	}

	public enum Key implements DataMapKey
	{
		ANNULATION_DATE, BOARDING, CODE, CONTENTS, DESCRIPTION, INFO_MEETING, INFORMATION, FIRST_DATE, INVITATION_DATE, INVITATION_DONE_DATE, LAST_ANNULATION_DATE, LAST_BOOKING_DATE, LAST_DATE, LODGING, MATERIAL_ORGANIZER, MATERIAL_PARTICIPANTS, MAX_AGE, MAX_PARTICIPANTS, MIN_AGE, MIN_PARTICIPANTS, PARTICIPANT_COUNT, PURPOSE, REALIZATION, RESPONSIBLE_USER, SEX_CONSTRAINT, STATE, TARGET_PUBLIC, TEASER, TITLE;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case ANNULATION_DATE:
				{
					return "Datum Annulation";
				}
				case BOARDING:
				{
					return "Verpflegung";
				}
				case CODE:
				{
					return "Kurscode";
				}
				case CONTENTS:
				{
					return "Kursinhalt";
				}
				case DESCRIPTION:
				{
					return "Kursbeschreibung";
				}
				case INFO_MEETING:
				{
					return "Informationstreffen";
				}
				case INFORMATION:
				{
					return "Informationen";
				}
				case FIRST_DATE:
				{
					return "Startdatum";
				}
				case INVITATION_DATE:
				{
					return "Datum Kurseinladung";
				}
				case INVITATION_DONE_DATE:
				{
					return "Datum erfolgte Kurseinladung";
				}
				case LAST_ANNULATION_DATE:
				{
					return "Spätestes Kursannulationsdatum";
				}
				case LAST_BOOKING_DATE:
				{
					return "Letztes mögliche Buchungsdatum";
				}
				case LAST_DATE:
				{
					return "Enddatum";
				}
				case LODGING:
				{
					return "Unterkunft";
				}
				case MATERIAL_ORGANIZER:
				{
					return "Material Kursanbieter";
				}
				case MATERIAL_PARTICIPANTS:
				{
					return "Material Teilnehmer";
				}
				case MAX_AGE:
				{
					return "Maximales Teilnahmealter";
				}
				case MAX_PARTICIPANTS:
				{
					return "Maximale Anzahl Teilnehmer";
				}
				case MIN_AGE:
				{
					return "Minimales Teilnahmealter";
				}
				case MIN_PARTICIPANTS:
				{
					return "Minimale Anzahl Teilnehmer";
				}
				case PARTICIPANT_COUNT:
				{
					return "Gesamtanzahl Teilnehmer";
				}
				case PURPOSE:
				{
					return "Kurszweck";
				}
				case REALIZATION:
				{
					return "Realisierung";
				}
				case RESPONSIBLE_USER:
				{
					return "Kursverantwortlicher";
				}
				case SEX_CONSTRAINT:
				{
					return "Eingrenzung Geschlecht";
				}
				case STATE:
				{
					return "Kursstatus";
				}
				case TARGET_PUBLIC:
				{
					return "Zielpublikum";
				}
				case TEASER:
				{
					return "Teaser";
				}
				case TITLE:
				{
					return "Kurstitel";
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
				case ANNULATION_DATE:
				{
					return "course_annulation_date";
				}
				case BOARDING:
				{
					return "course_boarding";
				}
				case CODE:
				{
					return "course_code";
				}
				case CONTENTS:
				{
					return "course_contents";
				}
				case DESCRIPTION:
				{
					return "course_description";
				}
				case INFO_MEETING:
				{
					return "course_info_meeting";
				}
				case INFORMATION:
				{
					return "course_informatioin";
				}
				case FIRST_DATE:
				{
					return "course_first_date";
				}
				case INVITATION_DATE:
				{
					return "course_invitation_date";
				}
				case INVITATION_DONE_DATE:
				{
					return "course_invitation_done_date";
				}
				case LAST_ANNULATION_DATE:
				{
					return "course_last_annulation_date";
				}
				case LAST_BOOKING_DATE:
				{
					return "course_booking_date";
				}
				case LAST_DATE:
				{
					return "course_last_date";
				}
				case LODGING:
				{
					return "course_lodging";
				}
				case MATERIAL_ORGANIZER:
				{
					return "course_material_organizer";
				}
				case MATERIAL_PARTICIPANTS:
				{
					return "course_material_participant";
				}
				case MAX_AGE:
				{
					return "course_max_age";
				}
				case MAX_PARTICIPANTS:
				{
					return "course_max_participants";
				}
				case MIN_AGE:
				{
					return "course_min_age";
				}
				case MIN_PARTICIPANTS:
				{
					return "course_min_participants";
				}
				case PARTICIPANT_COUNT:
				{
					return "course_participant_count";
				}
				case PURPOSE:
				{
					return "course_purpose";
				}
				case REALIZATION:
				{
					return "course_realization";
				}
				case RESPONSIBLE_USER:
				{
					return "course_responsible_user";
				}
				case SEX_CONSTRAINT:
				{
					return "course_sex_constraint";
				}
				case STATE:
				{
					return "couse_state";
				}
				case TARGET_PUBLIC:
				{
					return "course_target_public";
				}
				case TEASER:
				{
					return "course_teaser";
				}
				case TITLE:
				{
					return "course_title";
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
				case ANNULATION_DATE:
				{
					return "Annulation";
				}
				case BOARDING:
				{
					return "Verpflegung";
				}
				case CODE:
				{
					return "Code";
				}
				case CONTENTS:
				{
					return "Inhalt";
				}
				case DESCRIPTION:
				{
					return "Beschreibung";
				}
				case INFO_MEETING:
				{
					return "Informationstreffen";
				}
				case INFORMATION:
				{
					return "Informationen";
				}
				case FIRST_DATE:
				{
					return "Startdatum";
				}
				case INVITATION_DATE:
				{
					return "Kurseinladung";
				}
				case INVITATION_DONE_DATE:
				{
					return "Kurseinladung";
				}
				case LAST_ANNULATION_DATE:
				{
					return "Kursannulation bis";
				}
				case LAST_BOOKING_DATE:
				{
					return "Buchungen bis";
				}
				case LAST_DATE:
				{
					return "Enddatum";
				}
				case LODGING:
				{
					return "Unterkunft";
				}
				case MATERIAL_ORGANIZER:
				{
					return "Material Anbieter";
				}
				case MATERIAL_PARTICIPANTS:
				{
					return "Material Teilnehmer";
				}
				case MAX_AGE:
				{
					return "Max. Alter";
				}
				case MAX_PARTICIPANTS:
				{
					return "Max. Teilnehmerzahl";
				}
				case MIN_AGE:
				{
					return "Min. Alter";
				}
				case MIN_PARTICIPANTS:
				{
					return "Min. Teilnehmerzahl";
				}
				case PARTICIPANT_COUNT:
				{
					return "Teilnehmer";
				}
				case PURPOSE:
				{
					return "Kurszweck";
				}
				case REALIZATION:
				{
					return "Realisierung";
				}
				case RESPONSIBLE_USER:
				{
					return "Verantwortlicher";
				}
				case SEX_CONSTRAINT:
				{
					return "Geschlecht";
				}
				case STATE:
				{
					return "Kursstatus";
				}
				case TARGET_PUBLIC:
				{
					return "Zielpublikum";
				}
				case TEASER:
				{
					return "Teaser";
				}
				case TITLE:
				{
					return "Kurstitel";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Course course)
		{
			switch (this)
			{
				case ANNULATION_DATE:
				{
					Calendar calendar = course.getAnnulationDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case BOARDING:
				{
					return course.getBoarding();
				}
				case CODE:
				{
					return course.getCode();
				}
				case CONTENTS:
				{
					return course.getContents();
				}
				case DESCRIPTION:
				{
					return course.getDescription();
				}
				case INFO_MEETING:
				{
					return course.getInfoMeeting();
				}
				case INFORMATION:
				{
					return course.getInformation();
				}
				case FIRST_DATE:
				{
					Calendar firstDate = course.getFirstDate();
					if (firstDate == null)
					{
						return "";
					}
					if (firstDate.get(Calendar.HOUR_OF_DAY) == 0 && firstDate.get(Calendar.MINUTE) == 0)
					{
						return SimpleDateFormat.getDateInstance().format(firstDate.getTime());
					}
					return dateFormatter.format(firstDate.getTime());
				}
				case INVITATION_DATE:
				{
					Calendar calendar = course.getInvitationDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case INVITATION_DONE_DATE:
				{
					Calendar calendar = course.getInvitationDoneDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case LAST_ANNULATION_DATE:
				{
					Calendar calendar = course.getLastAnnulationDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case LAST_BOOKING_DATE:
				{
					Calendar calendar = course.getLastBookingDate();
					return calendar == null ? "" : dateFormatter.format(calendar.getTime());
				}
				case LAST_DATE:
				{
					Calendar lastDate = course.getLastDate();
					if (lastDate == null)
					{
						return "";
					}
					if (lastDate.get(Calendar.HOUR_OF_DAY) == 0 && lastDate.get(Calendar.MINUTE) == 0)
					{
						return SimpleDateFormat.getDateInstance().format(lastDate.getTime());
					}
					return dateFormatter.format(lastDate.getTime());
				}
				case LODGING:
				{
					return course.getLodging();
				}
				case MATERIAL_ORGANIZER:
				{
					return course.getMaterialOrganizer();
				}
				case MATERIAL_PARTICIPANTS:
				{
					return course.getMaterialParticipants();
				}
				case MAX_AGE:
				{
					return integerFormatter.format(course.getMaxAge());
				}
				case MAX_PARTICIPANTS:
				{
					return integerFormatter.format(course.getMaxParticipants());
				}
				case MIN_AGE:
				{
					return integerFormatter.format(course.getMinAge());
				}
				case MIN_PARTICIPANTS:
				{
					return integerFormatter.format(course.getMinParticipants());
				}
				case PARTICIPANT_COUNT:
				{
					return integerFormatter.format(course.getParticipantsCount());
				}
				case PURPOSE:
				{
					return course.getPurpose();
				}
				case REALIZATION:
				{
					return course.getRealization();
				}
				case RESPONSIBLE_USER:
				{
					User user = course.getResponsibleUser();
					return user == null ? "" : user.getFullname();
				}
				case SEX_CONSTRAINT:
				{
					return course.getSex() == null ? "" : course.getSex().toString();
				}
				case STATE:
				{
					return course.getState() == null ? "" : course.getState().toString();
				}
				case TARGET_PUBLIC:
				{
					return course.getTargetPublic();
				}
				case TEASER:
				{
					return course.getTeaser();
				}
				case TITLE:
				{
					return course.getTitle();
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
		BOOKINGS, BOOKING_TYPES, DETAILS, GUIDES;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case BOOKINGS:
				{
					return "Buchungen";
				}
				case BOOKING_TYPES:
				{
					return "Buchungsarten";
				}
				case DETAILS:
				{
					return "Kursdetails";
				}
				case GUIDES:
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
				case BOOKINGS:
				{
					return "course_bookings";
				}
				case BOOKING_TYPES:
				{
					return "course_booking_types";
				}
				case DETAILS:
				{
					return "course_details";
				}
				case GUIDES:
				{
					return "course_guides";
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
				case BOOKINGS:
				{
					return "Buchungen";
				}
				case BOOKING_TYPES:
				{
					return "Buchungsarten";
				}
				case DETAILS:
				{
					return "Details";
				}
				case GUIDES:
				{
					return "Leitung";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap> getTableMaps(final Course course)
		{
			switch (this)
			{
				case BOOKINGS:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					Collection<Booking> bookings = course.getBookings();
					for (Booking booking : bookings)
					{
						if (!booking.isDeleted())
						{
							tableMaps.add(new BookingMap(booking));
						}
					}
					return tableMaps;
				}
				case BOOKING_TYPES:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					Collection<BookingType> bookingTypes = course.getBookingTypes();
					for (BookingType bookingType : bookingTypes)
					{
						if (!bookingType.isDeleted())
						{
							tableMaps.add(new BookingTypeMap(bookingType));
						}
					}
					return tableMaps;
				}
				case DETAILS:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					Collection<CourseDetail> details = course.getCourseDetails();
					for (CourseDetail detail : details)
					{
						if (!detail.isDeleted())
						{
							tableMaps.add(new CourseDetailMap(detail));
						}
					}
					return tableMaps;
				}
				case GUIDES:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					Collection<CourseGuide> guides = course.getCourseGuides();
					for (CourseGuide guide : guides)
					{
						if (!guide.isDeleted())
						{
							tableMaps.add(new CourseGuideMap(guide));
						}
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
}
