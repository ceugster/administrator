package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.SchoolClass;
import ch.eugster.events.persistence.service.ConnectionService;

public class SchoolClassQuery extends AbstractEntityQuery<SchoolClass>
{

	public SchoolClassQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<SchoolClass> selectAll()
	{
		return this.selectAll(SchoolClass.class);
	}

	public boolean isNameUnique(String name, Long id)
	{
		if (name == null || name.length() == 0)
			return true;
		Expression expression = new ExpressionBuilder(SchoolClass.class).get("name").equal(name);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<SchoolClass> schoolClasses = select(SchoolClass.class, expression);
		if (schoolClasses.isEmpty())
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
				for (SchoolClass schoolClass : schoolClasses)
				{
					if (!schoolClass.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

}
