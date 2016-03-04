package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Rubric;
import ch.eugster.events.persistence.service.ConnectionService;

public class RubricQuery extends AbstractEntityQuery<Rubric>
{

	public RubricQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Rubric.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<Rubric> rubrics = select(Rubric.class, expression);
		if (rubrics.isEmpty())
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
				for (Rubric rubric : rubrics)
				{
					if (!rubric.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public List<Rubric> selectAll()
	{
		return super.selectAll(Rubric.class);
	}

}
