package ch.eugster.events.todo.service;

import java.util.List;

import org.osgi.service.component.ComponentContext;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.queries.AbstractEntityQuery;
import ch.eugster.events.persistence.queries.CourseQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class TodoCollectorService 
{
	private ConnectionService connectionService;
	
	protected void activate(final ComponentContext componentContext)
	{
		System.out.println();
	}

	protected void deactivate(final ComponentContext componentContext)
	{
	}

	protected void setConnectionService(ConnectionService connectionService)
	{
		this.connectionService = connectionService;
	}

	protected void unsetConnectionService(ConnectionService connectionService)
	{
		this.connectionService = null;
	}
	
	public List<Course> collectCoursesWithDueAdvanceNoticeDates(long start, long end)
	{
		CourseQuery query = (CourseQuery)connectionService.getQuery(Course.class);
		return query.selectByAdvanceNoticeDate(start, end);
	}
	
	public List<Course> collectCoursesWithDueInvitationDates(long start, long end)
	{
		CourseQuery query = (CourseQuery)connectionService.getQuery(Course.class);
		return query.selectByInvitationDate(start, end);
	}
	
	public AbstractEntity updateEntity(Class<AbstractEntity> clazz, AbstractEntity entity)
	{
		@SuppressWarnings("unchecked")
		AbstractEntityQuery<AbstractEntity> query = (AbstractEntityQuery<AbstractEntity>) connectionService.getQuery(clazz);
		return query.merge(entity);
	}
}
