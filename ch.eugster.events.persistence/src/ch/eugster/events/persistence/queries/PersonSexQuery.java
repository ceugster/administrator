package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonSexQuery extends AbstractEntityQuery<PersonSex>
{
	public PersonSexQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isNameUnique(final String name, final Long id)
	{
		if (name == null || name.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(PersonSex.class).get("salutation").equal(name);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<PersonSex> personSexes = select(PersonSex.class, expression);
		if (personSexes.isEmpty())
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
				for (PersonSex personSex : personSexes)
				{
					if (!personSex.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public List<PersonSex> selectAll()
	{
		return super.selectAll(PersonSex.class);
	}

}
