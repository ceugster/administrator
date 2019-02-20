package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.service.ConnectionService;

public class GuideQuery extends AbstractEntityQuery<Guide>
{
	public GuideQuery(final ConnectionService service)
	{
		super(service);
	}

	public List<Guide> selectAll()
	{
		return this.selectAll(Guide.class);
	}
	
	public List<Guide> selectValids()
	{
		Expression expression = new ExpressionBuilder(Guide.class).get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("link").get("deleted").equal(false));
		expression = expression.and(new ExpressionBuilder().get("link").get("person").get("deleted").equal(false));
		expression = expression.and(new ExpressionBuilder().get("link").get("address").get("deleted").equal(false));
		return this.select(Guide.class, expression);
	}
}
