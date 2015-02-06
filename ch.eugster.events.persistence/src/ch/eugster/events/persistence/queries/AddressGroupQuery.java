package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupQuery extends AbstractEntityQuery<AddressGroup>
{

	public AddressGroupQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(String code, Long id)
	{
		if (code == null || code.length() == 0)
			return true;
		Expression expression = new ExpressionBuilder(AddressGroup.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<AddressGroup> addressGroups = select(AddressGroup.class, expression);
		if (addressGroups.isEmpty())
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
				for (AddressGroup addressGroup : addressGroups)
				{
					if (!addressGroup.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

}
