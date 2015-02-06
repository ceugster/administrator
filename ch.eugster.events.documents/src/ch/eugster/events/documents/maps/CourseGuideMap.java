package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Compensation;
import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.GuideType;

public class CourseGuideMap extends AbstractDataMap
{
	protected CourseGuideMap() {
		super();
	}

	public CourseGuideMap(final CourseGuide courseGuide, boolean loadTables)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(courseGuide));
		}
		setProperties(new GuideMap(courseGuide.getGuide()).getProperties());
		setProperties(new CourseMap(courseGuide.getCourse(), loadTables).getProperties());
		if (loadTables)
		{
			for (TableKey key : TableKey.values())
			{
				this.addTableMaps(key.getKey(), key.getTableMaps(courseGuide));
			}
		}
	}

	protected void printReferences(Writer writer)
	{
		printHeader(writer, 2, "Referenzen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, "#guide", "Leitungspersonen");
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
		printCell(writer, null, TableKey.COMPENSATIONS.getKey());
		printCell(writer, "#pcompensation", TableKey.COMPENSATIONS.getName());
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, null, TableKey.COMPENSATIONS_SALARY.getKey());
		printCell(writer, "#pcompensation", TableKey.COMPENSATIONS_SALARY.getName());
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, null, TableKey.COMPENSATIONS_CHARGES.getKey());
		printCell(writer, "#pcompensation", TableKey.COMPENSATIONS_CHARGES.getName());
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, null, TableKey.OTHER_COURSE_GUIDES.getKey());
		printCell(writer, "#course_guide", TableKey.OTHER_COURSE_GUIDES.getName());
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, null, TableKey.COURSE_DETAILS.getKey());
		printCell(writer, "#course_detail", TableKey.COURSE_DETAILS.getName());
		endTableRow(writer);
		endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		GUIDE_TYPE, PHONE, STATE, NOTE, EMAIL;

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
				case NOTE:
				{
					return "Bemerkungen";
				}
				case EMAIL:
				{
					return "Email";
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
				case NOTE:
				{
					return "course_guide_note";
				}
				case EMAIL:
				{
					return "course_guide_email";
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
				case NOTE:
				{
					return "Bemerkungen";
				}
				case EMAIL:
				{
					return "Email";
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
				case NOTE:
				{
					return courseGuide.getNote();
				}
				case EMAIL:
				{
					return courseGuide.getEmail();
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
		COMPENSATIONS, COMPENSATIONS_SALARY, COMPENSATIONS_CHARGES, OTHER_COURSE_GUIDES, COURSE_DETAILS, COURSE_BOOKING_TYPES;

		@Override
		public String getDescription()
		{
			switch (this)
			{
			case COMPENSATIONS:
			{
				return "Tabelle Lohn/Spesen";
			}
			case COMPENSATIONS_SALARY:
			{
				return "Lohntabelle";
			}
			case COMPENSATIONS_CHARGES:
			{
				return "Spesentabelle";
			}
			case OTHER_COURSE_GUIDES:
			{
				return "Kursleitungstabelle";
			}
			case COURSE_DETAILS:
			{
				return "Tabelle Kursdetails";
			}
			case COURSE_BOOKING_TYPES:
			{
				return "Tabelle Buchungsarten";
			}
			default:
			{
				return "";
			}
			}
		}

		@Override
		public String getKey()
		{
			switch (this)
			{
			case COMPENSATIONS:
			{
				return "table_compensations";
			}
			case COMPENSATIONS_SALARY:
			{
				return "table_compensations_salary";
			}
			case COMPENSATIONS_CHARGES:
			{
				return "table_compensations_charges";
			}
			case OTHER_COURSE_GUIDES:
			{
				return "table_other_course_guides";
			}
			case COURSE_DETAILS:
			{
				return "table_course_details";
			}
			case COURSE_BOOKING_TYPES:
			{
				return "table_booking_types";
			}
			default:
			{
				return "";
			}
			}
		}

		@Override
		public String getName()
		{
			switch (this)
			{
			case COMPENSATIONS:
			{
				return "Tabelle Lohn/Spesen";
			}
			case COMPENSATIONS_SALARY:
			{
				return "Lohntabelle";
			}
			case COMPENSATIONS_CHARGES:
			{
				return "Spesentabelle";
			}
			case OTHER_COURSE_GUIDES:
			{
				return "Kursleitertabelle";
			}
			case COURSE_DETAILS:
			{
				return "Tabelle Kursdetails";
			}
			case COURSE_BOOKING_TYPES:
			{
				return "Tabelle Buchungsarten";
			}
			default:
			{
				return "";
			}
			}
		}

		public List<DataMap> getTableMaps(final CourseGuide courseGuide)
		{
			List<DataMap> tableMaps = new ArrayList<DataMap>();
			switch (this)
			{
			case COMPENSATIONS:
			{
				List<Compensation> compensations = courseGuide.getCompensations();
				for (Compensation compensation : compensations)
				{
					if (!compensation.isDeleted())
					{
						tableMaps.add(new CompensationMap(compensation));
					}
				}
				break;
			}
			case COMPENSATIONS_SALARY:
			{
				List<Compensation> compensations = courseGuide.getCompensations();
				for (Compensation compensation : compensations)
				{
					if (!compensation.isDeleted())
					{
						if (compensation.getCompensationType().getType().equals(CompensationType.Type.SALARY) ||
								compensation.getCompensationType().getType().equals(CompensationType.Type.SALARY_DISCOUNT))
						{
							tableMaps.add(new CompensationMap(compensation));
						}
					}
				}
				break;
			}
			case COMPENSATIONS_CHARGES:
			{
				List<Compensation> compensations = courseGuide.getCompensations();
				for (Compensation compensation : compensations)
				{
					if (!compensation.isDeleted())
					{
						if (compensation.getCompensationType().getType().equals(CompensationType.Type.CHARGE))
						{
							tableMaps.add(new CompensationMap(compensation));
						}
					}
				}
				break;
			}
			case OTHER_COURSE_GUIDES:
			{
				List<CourseGuide> courseGuides = courseGuide.getCourse().getCourseGuides();
				for (CourseGuide otherCourseGuide : courseGuides)
				{
					if (!otherCourseGuide.getId().equals(courseGuide.getId()))
					{
						tableMaps.add(new CourseGuideMap(otherCourseGuide, false));
					}
				}
				break;
			}
			case COURSE_DETAILS:
			{
				List<CourseDetail> courseDetails = courseGuide.getCourse().getCourseDetails();
				for (CourseDetail courseDetail : courseDetails)
				{
					if (!courseDetail.isDeleted())
					{
						tableMaps.add(new CourseDetailMap(courseDetail));
					}
				}
				break;
			}
			case COURSE_BOOKING_TYPES:
			{
				List<BookingType> bookingTypes = courseGuide.getCourse().getBookingTypes();
				for (BookingType bookingType : bookingTypes)
				{
					if (!bookingType.isDeleted())
					{
						tableMaps.add(new BookingTypeMap(bookingType));
					}
				}
			}
			}
			return tableMaps;
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
