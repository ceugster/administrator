package ch.eugster.events.documents.maps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.TodoEntry;

public class TodoMap extends AbstractDataMap {

	private static DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

	private static DateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	protected TodoMap() {
		super();
	}

	public TodoMap(final TodoEntry entry)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(entry));
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}

	public enum Key implements DataMapKey
	{
		DUE_DATE, TODO_TYPE, TITLE, COURSE_GUIDE, COURSE_DATE;

		@Override
		public String getDescription() 
		{
			switch (this)
			{
			case DUE_DATE:
			{
				return "Fällig";
			}
			case TODO_TYPE:
			{
				return "Aufgabe";
			}
			case TITLE:
			{
				return "Bezeichnung";
			}
			case COURSE_DATE:
			{
				return "Kursdaten";
			}
			case COURSE_GUIDE:
			{
				return "Kursleitung";
			}
			default:
			{
				throw new RuntimeException("Invalid key");
			}
			}
		}

		public String getValue(TodoEntry entry) 
		{
			switch (this)
			{
			case DUE_DATE:
			{
				return dateFormatter.format(entry.getDueDate().getTime());
			}
			case TODO_TYPE:
			{
				return entry.getDueType();
			}
			case TITLE:
			{
				return entry.getInstanceName();
			}
			case COURSE_DATE:
			{
				if (entry.getEntity() instanceof Course)
				{
					Calendar calendar = ((Course) entry.getEntity()).getFirstDate();
					return calendar == null ? "" : dateTimeFormatter.format(calendar.getTime());
				}
				return "";
			}
			case COURSE_GUIDE:
			{
				if (entry.getEntity() instanceof Course)
				{
					StringBuilder builder = new StringBuilder();
					List<CourseGuide> guides = ((Course) entry.getEntity()).getCourseGuides();
					for (CourseGuide guide : guides)
					{
						builder = builder.append(guide.getGuideType().getName() + ": ");
						builder = builder.append(PersonFormatter.getInstance().formatLastnameFirstname(guide.getGuide().getLink().getPerson()) + "\n");
					}
					return builder.toString().substring(0, builder.toString().length() - 1);
				}
				return "";
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
			case DUE_DATE:
			{
				return "due.date";
			}
			case TODO_TYPE:
			{
				return "todo.type";
			}
			case TITLE:
			{
				return "name";
			}
			case COURSE_DATE:
			{
				return "course.date";
			}
			case COURSE_GUIDE:
			{
				return "course.guide";
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
			case DUE_DATE:
			{
				return "Fällig";
			}
			case TODO_TYPE:
			{
				return "Aufgabe";
			}
			case TITLE:
			{
				return "Bezeichnung";
			}
			case COURSE_DATE:
			{
				return "Kursdaten";
			}
			case COURSE_GUIDE:
			{
				return "Kursleitung";
			}
			default:
			{
				throw new RuntimeException("Invalid key");
			}
			}
		}
		
	}
}
