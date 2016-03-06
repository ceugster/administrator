package ch.eugster.events.persistence.model;

import java.util.Calendar;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.queries.CourseQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class TodoEntry
{
	private AbstractEntity entity;

	private DueType dueType;

	public TodoEntry(AbstractEntity entity, DueType dueType)
	{
		this.entity = entity;
		this.dueType = dueType;
	}

	public String getEntityName()
	{
		return entity.getEntityName();
	}

	public String getInstanceName()
	{
		return entity.getInstanceName();
	}

	public String getDueTypeName()
	{
		return this.dueType.getDueTypeName();
	}

	public DueType getDueType()
	{
		return this.dueType;
	}

	public Calendar getDueDate()
	{
		return this.dueType.getDueDate(entity);
	}

	public boolean isDone()
	{
		switch (this.dueType)
		{
			case COURSE_ADVANCE_NOTICE_DATE:
			{
				if (entity instanceof Course)
				{
					return ((Course) entity).getAdvanceNoticeDoneDate() != null;
				}
			}
			case COURSE_INVITATION_DATE:
			{
				if (entity instanceof Course)
				{
					return ((Course) entity).getInvitationDoneDate() != null;
				}
			}
			default:
			{
				return false;
			}
		}
	}

	public Calendar getCourseBeginTime()
	{
		if (this.entity instanceof Course)
		{
			Course course = (Course) entity;
			return course.getFirstDate();
		}
		return null;
	}

	public void setEntity(AbstractEntity entity)
	{
		this.entity = entity;
	}

	public AbstractEntity getEntity()
	{
		return this.entity;
	}

	public enum DueType
	{
		COURSE_ADVANCE_NOTICE_DATE, COURSE_INVITATION_DATE;

		public String getDueTypeName()
		{
			switch (this)
			{
				case COURSE_ADVANCE_NOTICE_DATE:
				{
					return ("Voranzeige verschicken");
				}
				case COURSE_INVITATION_DATE:
				{
					return "Einladung verschicken";
				}
				default:
				{
					return null;
				}
			}
		}

		public Calendar getDueDate(AbstractEntity entity)
		{
			switch (this)
			{
				case COURSE_ADVANCE_NOTICE_DATE:
				{
					if (entity instanceof Course)
					{
						return ((Course) entity).getAdvanceNoticeDate();
					}
					return null;
				}
				case COURSE_INVITATION_DATE:
				{
					if (entity instanceof Course)
					{
						return ((Course) entity).getInvitationDate();
					}
					return null;
				}
				default:
				{
					return null;
				}
			}
		}

		public AbstractEntity update(AbstractEntity entity, boolean checked)
		{
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
			tracker.open();
			try
			{
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					switch (this)
					{
						case COURSE_ADVANCE_NOTICE_DATE:
						{
							Course course = (Course) entity;
							course.setAdvanceNoticeDoneDate(checked ? Calendar.getInstance() : null);
							CourseQuery query = (CourseQuery) service.getQuery(Course.class);
							return query.merge(course);
						}
						case COURSE_INVITATION_DATE:
						{
							Course course = (Course) entity;
							course.setInvitationDoneDate(checked ? Calendar.getInstance() : null);
							CourseQuery query = (CourseQuery) service.getQuery(Course.class);
							return query.merge(course);
						}
					}
				}
			}
			finally
			{
				tracker.close();
			}
			return null;
		}
	}
}
