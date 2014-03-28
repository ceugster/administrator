package ch.eugster.events.documents.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.eugster.events.persistence.model.Compensation;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.GuideType;

public class CourseGuideMap extends AbstractDataMap
{
	public CourseGuideMap(final CourseGuide courseGuide)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(courseGuide));
		}
		setProperties(new GuideMap(courseGuide.getGuide()).getProperties());
		setProperties(new CourseMap(courseGuide.getCourse()).getProperties());
	}

	public enum Key implements DataMapKey
	{
		GUIDE_TYPE, PHONE, STATE;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				// case DESCRIPTION:
				// {
				// return "Beschreibung";
				// }
				case GUIDE_TYPE:
				{
					return "Leitungsart";
				}
				case PHONE:
				{
					return "Telefon";
				}
				case STATE:
				{
					return "Leiterstatus";
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
				// case DESCRIPTION:
				// {
				// return "course_guide_description";
				// }
				case GUIDE_TYPE:
				{
					return "course_guide_type";
				}
				case PHONE:
				{
					return "course_guide_phone";
				}
				case STATE:
				{
					return "course_guide_state";
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
				// case DESCRIPTION:
				// {
				// return "Beschreibung";
				// }
				case GUIDE_TYPE:
				{
					return "Leitungsart";
				}
				case PHONE:
				{
					return "Telefon";
				}
				case STATE:
				{
					return "Status";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final CourseGuide courseGuide)
		{
			switch (this)
			{
				// case DESCRIPTION:
				// {
				// return courseGuide.getDescription();
				// }
				case GUIDE_TYPE:
				{
					GuideType type = courseGuide.getGuideType();
					return type == null ? "" : type.getName();
				}
				case PHONE:
				{
					return courseGuide.getPhone();
				}
				case STATE:
				{
					return courseGuide.getGuideType().getName();
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
		COMPENSATIONS;

		@Override
		public String getDescription()
		{
			return "Entschädigungen";
		}

		@Override
		public String getKey()
		{
			return "course_guide_compensations";
		}

		@Override
		public String getName()
		{
			return "Entschädigungen";
		}

		public List<DataMap> getTableMaps(final CourseGuide courseGuide)
		{
			List<DataMap> tableMaps = new ArrayList<DataMap>();
			Collection<Compensation> compensations = courseGuide.getCompensations();
			for (Compensation compensation : compensations)
			{
				tableMaps.add(new CompensationMap(compensation));
			}
			return tableMaps;
		}
	}
}
