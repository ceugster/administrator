package ch.eugster.events.persistence.queries;

import java.util.Collection;

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

	public boolean isCodeUnique(String code, Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Course.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		Collection<Course> courses = select(Course.class, expression);
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
