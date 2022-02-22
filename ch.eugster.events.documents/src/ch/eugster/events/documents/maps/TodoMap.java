package ch.eugster.events.documents.maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.persistence.model.TodoEntry;

public class TodoMap extends AbstractDataMap<AbstractEntity>
{
	private final Map<String, String> guideTypes = new HashMap<String, String>();

	protected TodoMap()
	{
		super();
	}

	public TodoMap(final TodoEntry todoEntry)
	{
		for (final TodoMap.Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(todoEntry));
		}
		if (todoEntry.getEntity() instanceof Course)
		{
			final Course course = (Course) todoEntry.getEntity();
			final List<CourseGuide> courseGuides = course.getCourseGuides();
			for (final CourseGuide courseGuide : courseGuides)
			{
				final String key = "guide.type." + courseGuide.getGuideType().getId().toString();
				final String value = this.guideTypes.remove(key);
				StringBuilder builder = new StringBuilder(value == null ? "" : value + "\n");
				builder = builder.append(PersonFormatter.getInstance().formatLastnameFirstname(courseGuide.getGuide().getLink().getPerson()));
				this.guideTypes.put(key, builder.toString());

				if (Key.getGuideType(courseGuide.getGuideType().getId()) == null)
				{
					Key.putGuideType(courseGuide.getGuideType());
				}
			}
			final Set<Entry<String, String>> entries = this.guideTypes.entrySet();
			final Iterator<Entry<String, String>> guides = entries.iterator();
			while (guides.hasNext())
			{
				final Entry<String, String> guide = guides.next();
				this.setProperty(guide.getKey(), guide.getValue());
			}
		}
	}

	@Override
	protected DataMapKey[] getKeys()
	{
		return Key.getKeys();
	}

	private static class GuideTypeKey implements DataMapKey, Comparable<GuideTypeKey>
	{
		private final GuideType guideType;

		public GuideTypeKey(final GuideType guideType)
		{
			this.guideType = guideType;
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public String getDescription()
		{
			return this.guideType.getDescription();
		}

		@Override
		public String getKey()
		{
			return "guide.type." + this.guideType.getId().toString();
		}

		@Override
		public String getName()
		{
			return this.guideType.getName();
		}

		@Override
		public int compareTo(final GuideTypeKey other)
		{
			final String code1 = this.guideType.getCode();
			final String code2 = other.guideType.getCode();
			return code1.compareTo(code2);
		}
	}

	public enum Key implements DataMapKey
	{
		DUE_DATE, TODO_TYPE, TITLE, COURSE_DATE;

		private static Map<Long, GuideType> guideTypes = new HashMap<Long, GuideType>();

		public static void clearGuideTypes()
		{
			Key.guideTypes.clear();
		}

		public static GuideType getGuideType(final Long key)
		{
			return Key.guideTypes.get(key);
		}

		public static void putGuideType(final GuideType guideType)
		{
			Key.guideTypes.put(guideType.getId(), guideType);
		}

		public static Map<Long, GuideType> getGuideTypes()
		{
			return Key.guideTypes;
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		public static DataMapKey[] getKeys()
		{
			final List<DataMapKey> keys = new ArrayList<DataMapKey>();
			for (final DataMapKey key : Key.values())
			{
				keys.add(key);
			}
			final GuideType[] types = Key.guideTypes.values().toArray(new GuideType[0]);
			Arrays.sort(types, new Comparator<GuideType>()
			{
				@Override
				public int compare(final GuideType type1, final GuideType type2)
				{
					return type1.getCode().compareTo(type2.getCode());
				}
			});
			for (final GuideType type : types)
			{
				keys.add(new GuideTypeKey(type));
			}
			return keys.toArray(new DataMapKey[0]);
		}

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
			default:
			{
				throw new RuntimeException("Invalid key");
			}
			}
		}

		public String getValue(final TodoEntry entry)
		{
			switch (this)
			{
			case DUE_DATE:
			{
				return AbstractDataMap.getDateFormatter().format(entry.getDueDate().getTime());
			}
			case TODO_TYPE:
			{
				return entry.getDueTypeName();
			}
			case TITLE:
			{
				return entry.getInstanceName();
			}
			case COURSE_DATE:
			{
				if (entry.getEntity() instanceof Course)
				{
					final Calendar calendar = ((Course) entry.getEntity()).getFirstDate();
					return calendar == null ? "" : AbstractDataMap.getDateTimeFormatter().format(calendar.getTime());
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
			default:
			{
				throw new RuntimeException("Invalid key");
			}
			}
		}

	}
}
