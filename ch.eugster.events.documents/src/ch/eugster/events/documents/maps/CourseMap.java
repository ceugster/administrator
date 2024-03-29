package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.User;

public class CourseMap extends AbstractDataMap<Course>
{
	private Course course;
	
	protected CourseMap() {
		super();
	}

	public CourseMap(final Course course)
	{
		this(course, false);
	}

	public CourseMap(final Course course, final boolean loadTables)
	{
		this.course = course;
		
		for (final Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(course));
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
			for (final TableKey key : TableKey.values())
			{
				this.addTableMaps(key.getKey(), key.getTableMaps(course));
			}
		}

	}

	public ch.eugster.events.documents.maps.DataMapKey[] getTableKeys()
	{
		return TableKey.values();
	}
	
	@Override
	protected void printReferences(final Writer writer)
	{
		this.printHeader(writer, 2, "Referenzen");
		this.startTable(writer, 0);
		this.startTableRow(writer);
		this.printCell(writer, "#season", "Seasons");
		this.endTableRow(writer);
		this.startTableRow(writer);
		this.printCell(writer, "#category", "Kurskategorien");
		this.endTableRow(writer);
		this.startTableRow(writer);
		this.printCell(writer, "#rubric", "Kursrubriken");
		this.endTableRow(writer);
		this.startTableRow(writer);
		this.printCell(writer, "#domain", "Domänen");
		this.endTableRow(writer);
		this.endTable(writer);
	}

	@Override
	protected void printTables(final Writer writer)
	{
		this.printHeader(writer, 2, "Tabellen");
		this.startTable(writer, 0);
		this.startTableRow(writer);
		this.printCell(writer, null, TableKey.BOOKINGS.getKey());
		this.printCell(writer, "#booking", TableKey.BOOKINGS.getName());
		this.endTableRow(writer);
		this.startTableRow(writer);
		this.printCell(writer, null, TableKey.BOOKING_TYPES.getKey());
		this.printCell(writer, "#booking_type", TableKey.BOOKING_TYPES.getName());
		this.endTableRow(writer);
		this.startTableRow(writer);
		this.printCell(writer, null, TableKey.DETAILS.getKey());
		this.printCell(writer, "#course_detail", TableKey.DETAILS.getName());
		this.endTableRow(writer);
		this.startTableRow(writer);
		this.printCell(writer, null, TableKey.GUIDES.getKey());
		this.printCell(writer, "#course_guide", TableKey.GUIDES.getName());
		this.endTableRow(writer);
		this.endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		ANNULATION_DATE, BOARDING, CODE, CONTENTS, DESCRIPTION, INFO_MEETING, INFORMATION, FIRST_DATE, INVITATION_DATE, INVITATION_DONE_DATE, LAST_ANNULATION_DATE, LAST_BOOKING_DATE, LAST_DATE, LODGING, MATERIAL_ORGANIZER, MATERIAL_PARTICIPANTS, MAX_AGE, MAX_PARTICIPANTS, MIN_AGE, MIN_PARTICIPANTS, PARTICIPANT_COUNT, PURPOSE, REALIZATION, COST_NOTE, RESPONSIBLE_USER, SEX_CONSTRAINT, STATE, TARGET_PUBLIC, TEASER, TITLE, PAYMENT_TERM, PREREQUISITES, SORTABLE_DATE, DATE_RANGE, DATE_RANGE_WITH_WEEKDAY_CODE, SUBSTITUTION_DATE_RANGE, SUBSTITUTION_DATE_RANGE_WITH_WEEKDAY_CODE, ALL_LOCATIONS, GUIDE_WITH_PROFESSION, ALL_BOOKING_TYPES, ADVANCE_NOTICE_DATE, ADVANCE_NOTICE_DONE_DATE;

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public String getDescription()
		{
			switch (this)
			{
			case ALL_BOOKING_TYPES:
			{
				return "Alle Buchungsarten";
			}
			case ALL_LOCATIONS:
			{
				return "Alle Durchführungsorte";
			}
			case ANNULATION_DATE:
			{
				return "Datum Annullation";
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
			case COST_NOTE:
			{
				return "Bemerkungen Kurskosten";
			}
			case SORTABLE_DATE:
			{
				return "Sortierdatum";
			}
			case DATE_RANGE:
			{
				return "Durchführungsdatum";
			}
			case DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				return "Durchführungsdatum mit Wochentagkürzel";
			}
			case DESCRIPTION:
			{
				return "Kursbeschreibung";
			}
			case FIRST_DATE:
			{
				return "Startdatum";
			}
			case GUIDE_WITH_PROFESSION:
			{
				return "Kursleitung mit Beruf";
			}
			case INFO_MEETING:
			{
				return "Informationstreffen";
			}
			case INFORMATION:
			{
				return "Informationen";
			}
			
			case INVITATION_DATE:
			{
				return "Datum Kurseinladung";
			}
			case INVITATION_DONE_DATE:
			{
				return "Datum erfolgte Kurseinladung";
			}
			case ADVANCE_NOTICE_DATE:
			{
				return "Datum Voranzeige";
			}
			case ADVANCE_NOTICE_DONE_DATE:
			{
				return "Datum erfolgte Voranzeige";
			}
			case LAST_ANNULATION_DATE:
			{
				return "Spätestes Kursannullationsdatum";
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
			case PAYMENT_TERM:
			{
				return "Zahlungsbedingungen";
			}
			case PREREQUISITES:
			{
				return "Voraussetzungen";
			}
			case PURPOSE:
			{
				return "Kurszweck";
			}
			case REALIZATION:
			{
				return "Durchführung";
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
			case SUBSTITUTION_DATE_RANGE:
			{
				return "Ersatzdatum";
			}
			case SUBSTITUTION_DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				return "Ersatzdatum mit Wochentag";
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
			case ALL_BOOKING_TYPES:
			{
				return "course_all_booking_types";
			}
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
				return "course_information";
			}
			case FIRST_DATE:
			{
				return "course_first_date";
			}
			case GUIDE_WITH_PROFESSION:
			{
				return "course_guide_with_profession";
			}
			case INVITATION_DATE:
			{
				return "course_invitation_date";
			}
			case INVITATION_DONE_DATE:
			{
				return "course_invitation_done_date";
			}
			case ADVANCE_NOTICE_DATE:
			{
				return "course_advance_notice_date";
			}
			case ADVANCE_NOTICE_DONE_DATE:
			{
				return "course_advance_notice_done_date";
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
			case COST_NOTE:
			{
				return "course_cost_note";
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
			case PAYMENT_TERM:
			{
				return "payment_term";
			}
			case PREREQUISITES:
			{
				return "course_prerequisites";
			}
			case SORTABLE_DATE:
			{
				return "course_date_range_sort";
			}
			case DATE_RANGE:
			{
				return "course_date_range";
			}
			case DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				return "course_date_range_with_weekday_code";
			}
			case SUBSTITUTION_DATE_RANGE:
			{
				return "course_substitution_date_range";
			}
			case SUBSTITUTION_DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				return "course_substitution_date_range_with_weekday_code";
			}
			case ALL_LOCATIONS:
			{
				return "course_all_locations";
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
			case ALL_BOOKING_TYPES:
			{
				return "Alle Buchungsarten";
			}
			case ANNULATION_DATE:
			{
				return "Annullation";
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
			case GUIDE_WITH_PROFESSION:
			{
				return "Kursleitung mit Beruf";
			}
			case INVITATION_DATE:
			{
				return "Kurseinladung";
			}
			case INVITATION_DONE_DATE:
			{
				return "Kurseinladung";
			}
			case ADVANCE_NOTICE_DATE:
			{
				return "Voranzeige";
			}
			case ADVANCE_NOTICE_DONE_DATE:
			{
				return "Voranzeige";
			}
			case LAST_ANNULATION_DATE:
			{
				return "Kursannullation bis";
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
			case COST_NOTE:
			{
				return "Bemerkungen Kurskosten";
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
			case PAYMENT_TERM:
			{
				return "Zahlungsbedingungen";
			}
			case PREREQUISITES:
			{
				return "Voraussetzungen";
			}
			case SORTABLE_DATE:
			{
				return "Sortierdatum";
			}
			case DATE_RANGE:
			{
				return "Durchführungsdatum";
			}
			case DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				return "Durchführungsdatum mit Wochentagkürzel";
			}
			case SUBSTITUTION_DATE_RANGE:
			{
				return "Ersatzdatum";
			}
			case SUBSTITUTION_DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				return "Ersatzdatum mit Wochentag";
			}
			case ALL_LOCATIONS:
			{
				return "Alle Durchführungsorte";
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
			case ALL_BOOKING_TYPES:
			{
				StringBuilder builder = new StringBuilder();
				final List<BookingType> bookingTypes = course.getBookingTypes();
				for (final BookingType bookingType : bookingTypes)
				{
					if (!bookingType.isDeleted())
					{
						if (builder.length() != 0)
						{
							builder = builder.append(", ");
						}
						builder = builder.append(bookingType.getName() + " " + AbstractDataMap.getAmountFormatter().format(bookingType.getPrice()));
					}
				}
				if (!course.getCostNote().isEmpty())
				{
					builder = builder.append(" (" + course.getCostNote() + ")");
				}
				return builder.toString();
			}
			case ANNULATION_DATE:
			{
				final Calendar calendar = course.getAnnulationDate();
				return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
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
			case GUIDE_WITH_PROFESSION:
			{
				StringBuilder builder = new StringBuilder();
				final List<CourseGuide> guides = course.getCourseGuides();
				for (final CourseGuide guide : guides)
				{
					if (!guide.getGuide().isDeleted() && !guide.isDeleted() && guide.getGuideType().isUseInPrints())
					{
						if (builder.length() != 0)
						{
							builder = builder.append("; ");
						}
						final Person person = guide.getGuide().getLink().getPerson();
						builder = builder.append(PersonFormatter.getInstance().formatFirstnameLastname(person));
						if (!person.getProfession().isEmpty())
						{
							builder = builder.append(", " + person.getProfession());
						}
					}
				}
				return builder.toString();
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
				final Calendar firstDate = course.getFirstDate();
				if (firstDate == null)
				{
					return "";
				}
				if (firstDate.get(Calendar.HOUR_OF_DAY) == 0 && firstDate.get(Calendar.MINUTE) == 0)
				{
					return DateFormat.getDateInstance().format(firstDate.getTime());
				}
				return AbstractDataMap.getDateFormatter().format(firstDate.getTime());
			}
			case INVITATION_DATE:
			{
				final Calendar calendar = course.getInvitationDate();
				return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
			}
			case INVITATION_DONE_DATE:
			{
				final Calendar calendar = course.getInvitationDoneDate();
				return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
			}
			case ADVANCE_NOTICE_DATE:
			{
				final Calendar calendar = course.getAdvanceNoticeDate();
				return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
			}
			case ADVANCE_NOTICE_DONE_DATE:
			{
				final Calendar calendar = course.getAdvanceNoticeDoneDate();
				return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
			}
			case LAST_ANNULATION_DATE:
			{
				final Calendar calendar = course.getLastAnnulationDate();
				return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
			}
			case LAST_BOOKING_DATE:
			{
				final Calendar calendar = course.getLastBookingDate();
				return calendar == null ? "" : AbstractDataMap.getDateFormatter().format(calendar.getTime());
			}
			case LAST_DATE:
			{
				final Calendar lastDate = course.getLastDate();
				if (lastDate == null)
				{
					return "";
				}
				if (lastDate.get(Calendar.HOUR_OF_DAY) == 0 && lastDate.get(Calendar.MINUTE) == 0)
				{
					return DateFormat.getDateInstance().format(lastDate.getTime());
				}
				return AbstractDataMap.getDateFormatter().format(lastDate.getTime());
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
				return AbstractDataMap.getIntegerFormatter().format(course.getMaxAge());
			}
			case MAX_PARTICIPANTS:
			{
				return AbstractDataMap.getIntegerFormatter().format(course.getMaxParticipants());
			}
			case MIN_AGE:
			{
				return AbstractDataMap.getIntegerFormatter().format(course.getMinAge());
			}
			case MIN_PARTICIPANTS:
			{
				return AbstractDataMap.getIntegerFormatter().format(course.getMinParticipants());
			}
			case PARTICIPANT_COUNT:
			{
				return AbstractDataMap.getIntegerFormatter().format(course.getParticipantsCount());
			}
			case PURPOSE:
			{
				return course.getPurpose();
			}
			case REALIZATION:
			{
				return course.getRealization();
			}
			case COST_NOTE:
			{
				return course.getCostNote();
			}
			case RESPONSIBLE_USER:
			{
				final User user = course.getResponsibleUser();
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
			case PAYMENT_TERM:
			{
				return course.getPaymentTerm() == null ? "" : course.getPaymentTerm().getText();
			}
			case PREREQUISITES:
			{
				return course.getPrerequisites();
			}
			case SORTABLE_DATE:
			{
				final Date date = course.getFirstDate() == null ? null : course.getFirstDate().getTime();
				return date == null ? "" : DateFormat.getDateTimeInstance().format(course.getFirstDate().getTime());
			}
			case DATE_RANGE:
			{
				final Calendar[] dates = CourseMap.getDates(course);
				return CourseMap.getDateRange(dates, WeekdayType.NONE);
			}
			case DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				final Calendar[] dates = CourseMap.getDates(course);
				return CourseMap.getDateRange(dates, WeekdayType.SHORT);
			}
			case SUBSTITUTION_DATE_RANGE:
			{
				final Calendar[] dates = CourseMap.getSubstitutionDates(course);
				return CourseMap.getDateRange(dates, WeekdayType.NONE);
			}
			case SUBSTITUTION_DATE_RANGE_WITH_WEEKDAY_CODE:
			{
				final Calendar[] dates = CourseMap.getSubstitutionDates(course);
				return CourseMap.getDateRange(dates, WeekdayType.SHORT);
			}
			case ALL_LOCATIONS:
			{
				StringBuilder builder = new StringBuilder();
				final List<CourseDetail> details = course.getCourseDetails();
				for (final CourseDetail detail : details)
				{
					if (!detail.isDeleted() && !detail.getLocation().isEmpty())
					{
						if (builder.length() == 0)
						{
							builder = builder.append(detail.getLocation());
						}
						else
						{
							builder = builder.append(", " + detail.getLocation());
						}
					}
				}
				return builder.toString();
			}
			default:
			{
				throw new RuntimeException("Invalid key");
			}
			}
		}

	}
	
	public static Calendar[] getDates(final Course course)
	{
		final Calendar[] dates = new Calendar[2];
		final List<CourseDetail> details = course.getCourseDetails();
		for (final CourseDetail detail : details)
		{
			if (!detail.isDeleted())
			{
				if (dates[0] == null || (detail.getStart() != null && detail.getStart().before(dates[0])))
				{
					dates[0] = detail.getStart();
				}
				if (dates[1] == null || (detail.getEnd() != null && detail.getEnd().after(dates[1])))
				{
					dates[1] = detail.getEnd();
				}
			}
		}
		return dates;
	}

	public static Calendar[] getSubstitutionDates(final Course course)
	{
		final Calendar[] dates = new Calendar[2];
		final List<CourseDetail> details = course.getCourseDetails();
		for (final CourseDetail detail : details)
		{
			if (!detail.isDeleted())
			{
				if (dates[0] == null || (detail.getSubstituteStart() != null && detail.getSubstituteStart().before(dates[0])))
				{
					dates[0] = detail.getSubstituteStart();
				}
				if (dates[1] == null || (detail.getSubstituteEnd() != null && detail.getSubstituteEnd().after(dates[1])))
				{
					dates[1] = detail.getSubstituteEnd();
				}
			}
		}
		return dates;
	}

	public static String getDateRange(final Calendar[] dates, final WeekdayType weekdayType)
	{
		if (dates[0] != null && dates[1] != null)
		{
			if (dates[0].get(Calendar.YEAR) == dates[1].get(Calendar.YEAR))
			{
				if (dates[0].get(Calendar.MONTH) == dates[1].get(Calendar.MONTH))
				{
					if (dates[0].get(Calendar.DAY_OF_MONTH) == dates[1].get(Calendar.DAY_OF_MONTH))
					{
						final String start = CourseMap.getFormattedDate(dates[0], "dd. MMMM yyyy, HH:mm", weekdayType);
						final String end = CourseMap.getFormattedDate(dates[1], "HH:mm", WeekdayType.NONE);
						return start + " - " + end;
					}
					else
					{
						final String start = CourseMap.getFormattedDate(dates[0], "dd.", weekdayType);
						final String end = CourseMap.getFormattedDate(dates[1], "dd. MMMM yyyy,", weekdayType);
						final String startTime = CourseMap.getFormattedDate(dates[0], "HH:mm", WeekdayType.NONE);
						final String endTime = CourseMap.getFormattedDate(dates[1], "HH:mm", WeekdayType.NONE);
						return start + " - " + end + " " + startTime + " - " + endTime;
					}
				}
				else
				{
					final String start = CourseMap.getFormattedDate(dates[0], "dd. MMMM", weekdayType);
					final String end = CourseMap.getFormattedDate(dates[1], "dd. MMMM yyyy,", weekdayType);
					final String startTime = CourseMap.getFormattedDate(dates[0], "HH:mm", WeekdayType.NONE);
					final String endTime = CourseMap.getFormattedDate(dates[1], "HH:mm", WeekdayType.NONE);
					return start + " - " + end + " " + startTime + " - " + endTime;
				}
			}
			else
			{
				final String start = CourseMap.getFormattedDate(dates[0], "dd. MMMM yyyy", weekdayType);
				final String end = CourseMap.getFormattedDate(dates[1], "dd. MMMM yyyy,", weekdayType);
				final String startTime = CourseMap.getFormattedDate(dates[0], "HH:mm", WeekdayType.NONE);
				final String endTime = CourseMap.getFormattedDate(dates[1], "HH:mm", WeekdayType.NONE);
				return start + " - " + end + " " + startTime + " - " + endTime;
			}
		}
		if (dates[0] != null)
		{
			return CourseMap.getFormattedDate(dates[0], "dd. MMMM yyyy, HH:mm", weekdayType);
		}
		if (dates[1] != null)
		{
			return CourseMap.getFormattedDate(dates[1], "dd. MMMM yyyy, HH:mm", weekdayType);
		}
		return "";
	}

	public static String getFormattedDate(final Calendar date, final String format, final WeekdayType weekdayType)
	{
		final DateFormat formatter = new SimpleDateFormat(format);
		final String wd = Weekday.getWeekday(date.get(Calendar.DAY_OF_WEEK) - 1).getWeekday(weekdayType);
		final String formattedDate = formatter.format(date.getTime());
		return (wd.isEmpty() ? "" : wd + ", ") + formattedDate;
	}
	
	public enum TableKey implements DataMapKey
	{
		BOOKINGS, BOOKING_TYPES, DETAILS, GUIDES;

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

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
					return "table_course_bookings";
				}
				case BOOKING_TYPES:
				{
					return "table_booking_types";
				}
				case DETAILS:
				{
					return "table_course_details";
				}
				case GUIDES:
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

		public List<DataMap<?>> getTableMaps(final Course course)
		{
			switch (this)
			{
				case BOOKINGS:
				{
					final List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
					final List<Booking> bookings = course.getBookings();
					for (final Booking booking : bookings)
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
					final List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
					final List<BookingType> bookingTypes = course.getBookingTypes();
					for (final BookingType bookingType : bookingTypes)
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
					final List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
					final List<CourseDetail> details = course.getCourseDetails();
					for (final CourseDetail detail : details)
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
					final List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
					final List<CourseGuide> guides = course.getCourseGuides();
					Collections.sort(guides);
					for (final CourseGuide guide : guides)
					{
						if (!guide.isDeleted())
						{
							tableMaps.add(new CourseGuideMap(guide, false));
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

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}

	@Override
	public int compareTo(final DataMap<Course> other) 
	{
		final CourseMap otherMap = (CourseMap) other;
		final Calendar value1 =  this.course == null ? null :  this.course.getFirstDate();
		final Calendar value2 = otherMap.course == null ? null : otherMap.course.getFirstDate();
		final Date date1 = value1 == null ? null : value1.getTime();
		final Date date2 = value2 == null ? null : value2.getTime();
		if (date1 == null && date2 == null)
		{
			return 0;
		}
		if (date1 == null)
		{
			return -1;
		}
		if (date2 == null)
		{
			return 1;
		}
		return date1.compareTo(date2);
	}
}
