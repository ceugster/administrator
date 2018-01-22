package ch.eugster.events.documents.maps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ch.eugster.events.persistence.model.CourseDetail;

public class CourseDetailMap extends AbstractDataMap<CourseDetail>
{
	private static final DateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	private static final DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

	private static final DateFormat timeFormatter = new SimpleDateFormat("HH:mm");

	protected CourseDetailMap() {
		super();
	}

	public CourseDetailMap(final CourseDetail courseDetail)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(courseDetail));
		}
		// setProperties(new
		// CourseMap(courseDetail.getCourse()).getProperties());
	}

	public enum Key implements DataMapKey
	{
		START_DATE_TIME, START_DATE, START_TIME, END_DATE_TIME, END_DATE, END_TIME, JOURNEY, LOCATION, MEETING_POINT, SUBSTITUTION_START_DATE_TIME, SUBSTITUTION_START_DATE, SUBSTITUTION_START_TIME, SUBSTITUTION_END_DATE_TIME, SUBSTITUTION_END_DATE, SUBSTITUTION_END_TIME;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case START_DATE_TIME:
				{
					return "Beginn des Kursabschnitts (Datum und Zeit)";
				}
				case START_DATE:
				{
					return "Beginn des Kursabschnitts (Datum)";
				}
				case START_TIME:
				{
					return "Beginn des Kursabschnitts (Zeit)";
				}
				case END_DATE_TIME:
				{
					return "Ende des Kursabschnitts (Datum und Zeit)";
				}
				case END_DATE:
				{
					return "Ende des Kursabschnitts (Datum)";
				}
				case END_TIME:
				{
					return "Ende des Kursabschnitts (Zeit)";
				}
				case JOURNEY:
				{
					return "Anfahrtsinformationen";
				}
				case LOCATION:
				{
					return "Kursort";
				}
				case MEETING_POINT:
				{
					return "Treffpunkt";
				}
				case SUBSTITUTION_START_DATE_TIME:
				{
					return "Beginn des Kursabschnitts (Ersatzdatum und -Zeit)";
				}
				case SUBSTITUTION_START_DATE:
				{
					return "Beginn des Kursabschnitts (Ersatzdatum)";
				}
				case SUBSTITUTION_START_TIME:
				{
					return "Beginn des Kursabschnitts (Ersatzzeit)";
				}
				case SUBSTITUTION_END_DATE_TIME:
				{
					return "Ende des Kursabschnitts (Ersatzdatum und -Zeit)";
				}
				case SUBSTITUTION_END_DATE:
				{
					return "Ende des Kursabschnitts (Ersatzdatum)";
				}
				case SUBSTITUTION_END_TIME:
				{
					return "Ende des Kursabschnitts (Ersatzzeit)";
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
				case START_DATE_TIME:
				{
					return "course_detail_start_date_time";
				}
				case START_DATE:
				{
					return "course_detail_start_date";
				}
				case START_TIME:
				{
					return "course_detail_start_time";
				}
				case END_DATE_TIME:
				{
					return "course_detail_end_date_time";
				}
				case END_DATE:
				{
					return "course_detail_end_date";
				}
				case END_TIME:
				{
					return "course_detail_end_time";
				}
				case JOURNEY:
				{
					return "course_detail_journey";
				}
				case LOCATION:
				{
					return "course_detail_location";
				}
				case MEETING_POINT:
				{
					return "course_detail_meeting_point";
				}
				case SUBSTITUTION_START_DATE_TIME:
				{
					return "course_detail_substitution_start_date_time";
				}
				case SUBSTITUTION_START_DATE:
				{
					return "course_detail_substitution_start_date";
				}
				case SUBSTITUTION_START_TIME:
				{
					return "course_detail_substitution_start_time";
				}
				case SUBSTITUTION_END_DATE_TIME:
				{
					return "course_detail_substitution_end_date_time";
				}
				case SUBSTITUTION_END_DATE:
				{
					return "course_detail_substitution_end_date";
				}
				case SUBSTITUTION_END_TIME:
				{
					return "course_detail_substitution_end_time";
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
				case START_DATE_TIME:
				{
					return "Start (Datum und Zeit)";
				}
				case START_DATE:
				{
					return "Start (Datum)";
				}
				case START_TIME:
				{
					return "Start (Zeit)";
				}
				case END_DATE_TIME:
				{
					return "Ende (Datum und Zeit)";
				}
				case END_DATE:
				{
					return "Ende (Datum)";
				}
				case END_TIME:
				{
					return "Ende (Zeit)";
				}
				case JOURNEY:
				{
					return "Anfahrtsinformationen";
				}
				case LOCATION:
				{
					return "Kursort";
				}
				case MEETING_POINT:
				{
					return "Treffpunkt";
				}
				case SUBSTITUTION_START_DATE_TIME:
				{
					return "Beginn Ersatzdatum (Datum und Zeit)";
				}
				case SUBSTITUTION_START_DATE:
				{
					return "Beginn Ersatzdatum (Datum)";
				}
				case SUBSTITUTION_START_TIME:
				{
					return "Beginn Ersatzdatum (Zeit)";
				}
				case SUBSTITUTION_END_DATE_TIME:
				{
					return "Ende Ersatzdatum (Datum und Zeit)";
				}
				case SUBSTITUTION_END_DATE:
				{
					return "Ende Ersatzdatum (Datum)";
				}
				case SUBSTITUTION_END_TIME:
				{
					return "Ende Ersatzdatum (Zeit)";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		private String getDateTime(Calendar calendar)
		{
			if (calendar == null)
			{
				return "";
			}
			return dateTimeFormatter.format(calendar.getTime());
		}

		private String getDate(Calendar calendar)
		{
			if (calendar == null)
			{
				return "";
			}
			return dateFormatter.format(calendar.getTime());
		}

		private String getTime(Calendar calendar)
		{
			if (calendar == null)
			{
				return "";
			}
			return timeFormatter.format(calendar.getTime());
		}

		public String getValue(final CourseDetail courseDetail)
		{
			switch (this)
			{
				case START_DATE_TIME:
				{
					return getDateTime(courseDetail.getStart());
				}
				case START_DATE:
				{
					return getDate(courseDetail.getStart());
				}
				case START_TIME:
				{
					return getTime(courseDetail.getStart());
				}
				case END_DATE_TIME:
				{
					return getDateTime(courseDetail.getEnd());
				}
				case END_DATE:
				{
					return getDate(courseDetail.getEnd());
				}
				case END_TIME:
				{
					return getTime(courseDetail.getEnd());
				}
				case JOURNEY:
				{
					return courseDetail.getJourney();
				}
				case LOCATION:
				{
					return courseDetail.getLocation();
				}
				case MEETING_POINT:
				{
					return courseDetail.getMeetingPoint();
				}
				case SUBSTITUTION_START_DATE_TIME:
				{
					Calendar calendar = courseDetail.getSubstituteStart();
					return calendar == null ? "" : getDateTime(courseDetail.getSubstituteStart());
				}
				case SUBSTITUTION_START_DATE:
				{
					Calendar calendar = courseDetail.getSubstituteStart();
					return calendar == null ? "" : getDate(courseDetail.getSubstituteStart());
				}
				case SUBSTITUTION_START_TIME:
				{
					Calendar calendar = courseDetail.getSubstituteStart();
					return calendar == null ? "" : getTime(courseDetail.getSubstituteStart());
				}
				case SUBSTITUTION_END_DATE_TIME:
				{
					Calendar calendar = courseDetail.getSubstituteStart();
					return calendar == null ? "" : getDateTime(courseDetail.getSubstituteEnd());
				}
				case SUBSTITUTION_END_DATE:
				{
					Calendar calendar = courseDetail.getSubstituteStart();
					return calendar == null ? "" : getDate(courseDetail.getSubstituteEnd());
				}
				case SUBSTITUTION_END_TIME:
				{
					Calendar calendar = courseDetail.getSubstituteStart();
					return calendar == null ? "" : getTime(courseDetail.getSubstituteEnd());
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
