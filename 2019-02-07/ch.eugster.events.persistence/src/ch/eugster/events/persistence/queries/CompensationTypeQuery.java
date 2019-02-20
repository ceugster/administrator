package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.service.ConnectionService;

public class CompensationTypeQuery extends AbstractEntityQuery<CompensationType>
{

	public CompensationTypeQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(CompensationType.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<CompensationType> compensationTypes = select(CompensationType.class, expression);
		if (compensationTypes.isEmpty())
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
				for (CompensationType compensationType : compensationTypes)
				{
					if (!compensationType.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public List<CompensationType> selectAll()
	{
		return super.selectAll(CompensationType.class);
	}
}
