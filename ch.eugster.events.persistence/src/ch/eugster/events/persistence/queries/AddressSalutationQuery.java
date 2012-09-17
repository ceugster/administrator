package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressSalutationQuery extends AbstractEntityQuery<AddressSalutation>
{
	public AddressSalutationQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isNameUnique(final String name, final Long id)
	{
		if (name == null || name.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(AddressSalutation.class).get("salutation").equal(name);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		Collection<AddressSalutation> addressSalutations = select(AddressSalutation.class, expression);
		if (addressSalutations.isEmpty())
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
				for (AddressSalutation addressSalutation : addressSalutations)
				{
					if (!addressSalutation.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public Collection<AddressSalutation> selectAll()
	{
		return super.selectAll(AddressSalutation.class);
	}

}
