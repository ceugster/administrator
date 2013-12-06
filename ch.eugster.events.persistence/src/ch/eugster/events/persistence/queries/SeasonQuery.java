package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.service.ConnectionService;

public class SeasonQuery extends AbstractEntityQuery<Season>
{

	public SeasonQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Season.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		Collection<Season> seasons = select(Season.class, expression);
		if (seasons.isEmpty())
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
				for (Season season : seasons)
				{
					if (!season.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public Collection<Season> selectAll()
	{
		return super.selectAll(Season.class);
	}

	public Collection<Season> selectClosed(final boolean closed)
	{
		Expression expression = new ExpressionBuilder(Season.class).get("closed").equal(closed)
				.and(new ExpressionBuilder().get("deleted").equal(false));
		return this.select(Season.class, expression);
	}

}
