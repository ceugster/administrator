package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.BookingTypeProposition;
import ch.eugster.events.persistence.service.ConnectionService;

public class BookingTypePropositionQuery extends AbstractEntityQuery<BookingTypeProposition>
{

	public BookingTypePropositionQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<BookingTypeProposition> selectAll()
	{
		Expression expression = new ExpressionBuilder(BookingTypeProposition.class).get("deleted").equal(false);
		return select(BookingTypeProposition.class, expression);
	}

}
