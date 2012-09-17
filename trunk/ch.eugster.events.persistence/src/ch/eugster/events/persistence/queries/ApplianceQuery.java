package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.persistence.service.ConnectionService;

public class ApplianceQuery extends AbstractEntityQuery<Appliance>
{

	public ApplianceQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public Collection<Appliance> selectAll()
	{
		return this.selectAll(Appliance.class);
	}

	public boolean isNameUnique(String name, Long id)
	{
		if (name == null || name.length() == 0)
			return true;
		Expression expression = new ExpressionBuilder(Appliance.class).get("name").equal(name);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		Collection<Appliance> appliances = select(Appliance.class, expression);
		if (appliances.isEmpty())
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
				for (Appliance appliance : appliances)
				{
					if (!appliance.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

}
