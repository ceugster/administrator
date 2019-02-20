package ch.eugster.events.persistence.queries;

import java.util.List;

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
		List<AddressSalutation> addressSalutations = select(AddressSalutation.class, expression);
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

	public List<AddressSalutation> selectBySalutation(String salutation)
	{
		Expression expression = new ExpressionBuilder(AddressSalutation.class).get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("salutation").equal(salutation));
		return this.select(AddressSalutation.class, expression);
	}
	
	public List<AddressSalutation> selectAll()
	{
		return super.selectAll(AddressSalutation.class);
	}

}
