package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.persistence.service.ConnectionService;

public class SchoolLevelQuery extends AbstractEntityQuery<SchoolLevel>
{

	public SchoolLevelQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<SchoolLevel> selectAll()
	{
		return this.selectAll(SchoolLevel.class);
	}

	public boolean isNameUnique(String name, Long id)
	{
		if (name == null || name.length() == 0)
			return true;
		Expression expression = new ExpressionBuilder(SchoolLevel.class).get("name").equal(name);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<SchoolLevel> schoolLevels= select(SchoolLevel.class, expression);
		if (schoolLevels.isEmpty())
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
				for (SchoolLevel schoolLevel : schoolLevels)
				{
					if (!schoolLevel.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

}
