package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.persistence.service.ConnectionService;

public class CharityRunQuery extends AbstractEntityQuery<CharityRun>
{

	public CharityRunQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<CharityRun> selectActives()
	{
		final Expression expression = new ExpressionBuilder(CharityRun.class).get("deleted").equal(false);
		final List<CharityRun> charityRuns = this.select(CharityRun.class, expression);
		return charityRuns;
	}

}
