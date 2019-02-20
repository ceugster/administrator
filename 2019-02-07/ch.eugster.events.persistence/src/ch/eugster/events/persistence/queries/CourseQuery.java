package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseQuery extends AbstractEntityQuery<Course>
{
	public CourseQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}
	
	public List<Course> selectByAdvanceNoticeDate(long start, long end, boolean open, boolean done)
	{
		Calendar startDate = GregorianCalendar.getInstance();
		startDate.setTimeInMillis(start);
		Calendar endDate = GregorianCalendar.getInstance();
		endDate.setTimeInMillis(end);
		Expression expression = new ExpressionBuilder(Course.class).get("advanceNoticeDate").between(startDate, endDate);
		if (open && done)
		{
		}
		else if (open)
		{
			expression = expression.and(new ExpressionBuilder().get("advanceNoticeDoneDate").isNull());
		}
		else if (done)
		{
			expression = expression.and(new ExpressionBuilder().get("advanceNoticeDoneDate").notNull());
		}
		else
		{
			return new ArrayList<Course>();
		}
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return select(Course.class, expression);
	}

	public List<Course> selectByInvitationDate(long start, long end, boolean open, boolean done)
	{
		Calendar startDate = GregorianCalendar.getInstance();
		startDate.setTimeInMillis(start);
		Calendar endDate = GregorianCalendar.getInstance();
		endDate.setTimeInMillis(end);
		Expression expression = new ExpressionBuilder(Course.class).get("invitationDate").between(startDate, endDate);
		if (open && done)
		{
		}
		else if (open)
		{
			expression = expression.and(new ExpressionBuilder().get("invitationDoneDate").isNull());
		}
		else if (done)
		{
			expression = expression.and(new ExpressionBuilder().get("invitationDoneDate").notNull());
		}
		else
		{
			return new ArrayList<Course>();
		}
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return select(Course.class, expression);
	}

	public boolean isCodeUnique(String code, Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Course.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<Course> courses = select(Course.class, expression);
		if (courses.isEmpty())
		{
			return true;
		}
		else
		{
			if (id == null)
			{
				return false;
			}
			else
			{
				for (Course course : courses)
				{
					if (!course.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public long count(Domain domain)
	{
		Expression expression = new ExpressionBuilder(Course.class).get("domain").equal(domain);
		return count(Course.class, expression);
	}
}
