package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.CharityRunner;
import ch.eugster.events.persistence.service.ConnectionService;

public class CharityRunnerQuery extends AbstractEntityQuery<CharityRunner>
{

	public CharityRunnerQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<CharityRunner> selectLeaders()
	{
		Expression expression = new ExpressionBuilder(CharityRunner.class).get("leadership").equal(true);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<CharityRunner> charityRunners = select(CharityRunner.class, expression);
		return charityRunners;
	}

}
