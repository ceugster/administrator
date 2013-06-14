package ch.eugster.events.documents.maps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ch.eugster.events.persistence.model.CourseDetail;

public class CourseDetailMap extends AbstractDataMap
{
	private static DateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	private static DateFormat dateFormatter = SimpleDateFormat.getDateInstance();

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
		END, JOURNEY, LOCATION, MEETING_POINT, START, SUBSTITUTION_START, SUBSTITUTION_END;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case END:
				{
					return "Ende des Kursabschnitts (Datum und Zeit)";
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
				case START:
				{
					return "Beginn des Kursabschnitts (Datum und Zeit)";
				}
				case SUBSTITUTION_START:
				{
					return "Beginn des Kursabschnitts (Ersatzdatum und -Zeit)";
				}
				case SUBSTITUTION_END:
				{
					return "Ende des Kursabschnitts (Ersatzdatum und -Zeit)";
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
				case END:
				{
					return "course_detail_end";
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
				case START:
				{
					return "course_detail_start";
				}
				case SUBSTITUTION_START:
				{
					return "course_detail_substitution_start";
				}
				case SUBSTITUTION_END:
				{
					return "course_detail_substitution_end";
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
				case END:
				{
					return "Ende";
				}
				case JOURNEY:
				{
					return "Anfahrt";
				}
				case LOCATION:
				{
					return "Kursort";
				}
				case MEETING_POINT:
				{
					return "Treffpunkt";
				}
				case START:
				{
					return "Beginn";
				}
				case SUBSTITUTION_START:
				{
					return "Beginn (Ersatzdatum)";
				}
				case SUBSTITUTION_END:
				{
					return "Ende (Ersatzdatum)";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final CourseDetail courseDetail)
		{
			switch (this)
			{
				case END:
				{
					Calendar end = courseDetail.getEnd();
					if (end == null)
					{
						return "";
					}
					if (end.get(Calendar.HOUR_OF_DAY) == 0 && end.get(Calendar.MINUTE) == 0)
					{
						return dateFormatter.format(end.getTime());
					}
					return dateTimeFormatter.format(end.getTime());
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
				case START:
				{
					Calendar start = courseDetail.getStart();
					if (start == null)
					{
						return "";
					}
					if (start.get(Calendar.HOUR_OF_DAY) == 0 && start.get(Calendar.MINUTE) == 0)
					{
						return dateFormatter.format(start.getTime());
					}
					return dateTimeFormatter.format(start.getTime());
				}
				case SUBSTITUTION_START:
				{
					Calendar calendar = courseDetail.getSubstituteStart();
					return calendar == null ? "" : dateTimeFormatter.format(calendar.getTime());
				}
				case SUBSTITUTION_END:
				{
					Calendar calendar = courseDetail.getSubstituteEnd();
					return calendar == null ? "" : dateTimeFormatter.format(calendar.getTime());
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}
}
