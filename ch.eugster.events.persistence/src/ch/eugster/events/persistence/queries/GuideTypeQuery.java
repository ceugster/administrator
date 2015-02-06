package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.persistence.service.ConnectionService;

public class GuideTypeQuery extends AbstractEntityQuery<GuideType>
{

	public GuideTypeQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(GuideType.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<GuideType> guideTypes = select(GuideType.class, expression);
		if (guideTypes.isEmpty())
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
				for (GuideType guideType : guideTypes)
				{
					if (!guideType.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public List<GuideType> selectAll()
	{
		return super.selectAll(GuideType.class);
	}
}
