package ch.eugster.events.persistence.queries;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.SchoolClass;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.service.ConnectionService;

public class VisitQuery extends AbstractEntityQuery<Visit>
{
	public VisitQuery(ConnectionService service)
	{
		super(service);
	}

	public long countSchoolClasses(SchoolClass schoolClass)
	{
		Expression expression = new ExpressionBuilder(Visit.class).get("schoolClass").equal(schoolClass);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return this.count(Visit.class, expression);
	}

	public Collection<Visit> selectAppointed()
	{
		Expression expression = new ExpressionBuilder(Visit.class).get("start").notNull();
		expression = expression.and(new ExpressionBuilder().get("end").notNull());
		return this.select(Visit.class, expression);
	}

	public Collection<Visit> selectAfterLastYear()
	{
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DATE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Expression expression = new ExpressionBuilder(Visit.class).get("end").isNull()
				.or(new ExpressionBuilder().get("end").greaterThanEqual(calendar));
		Collection<Visit> visits = this.select(Visit.class, expression);
		return visits;
	}

	public Collection<Visit> selectAll()
	{
		return this.selectAll(Visit.class);
	}
}
