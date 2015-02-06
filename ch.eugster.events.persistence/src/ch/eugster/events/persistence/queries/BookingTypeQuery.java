package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.service.ConnectionService;

public class BookingTypeQuery extends AbstractEntityQuery<BookingType>
{

	public BookingTypeQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<BookingType> selectByCourse(Course course)
	{
		Expression expression = new ExpressionBuilder(BookingType.class).get("course").equal(course);
		return select(BookingType.class, expression);
	}

}
