package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.service.ConnectionService;

public class VisitThemeQuery extends AbstractEntityQuery<VisitTheme>
{

	public VisitThemeQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isNameUnique(String name, Long id)
	{
		if (name == null || name.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(VisitTheme.class).get("name").equal(name);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<VisitTheme> visitThemes = this.select(VisitTheme.class, expression);
		if (visitThemes.isEmpty())
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
				for (VisitTheme visitTheme : visitThemes)
				{
					if (!visitTheme.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

}
