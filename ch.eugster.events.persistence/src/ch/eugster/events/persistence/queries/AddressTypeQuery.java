package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressTypeQuery extends AbstractEntityQuery<AddressType>
{
	public AddressTypeQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isNameUnique(final String name, final Long id)
	{
		if (name == null || name.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(AddressType.class).get("name").equal(name);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		Collection<AddressType> addressTypes = select(AddressType.class, expression);
		if (addressTypes.isEmpty())
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
				for (AddressType addressType : addressTypes)
				{
					if (!addressType.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public Collection<AddressType> selectAll()
	{
		return selectAll(true);
	}

	public Collection<AddressType> selectAll(final boolean deletedToo)
	{
		Collection<AddressType> addressTypes = super.selectAll(AddressType.class, deletedToo);
		if (addressTypes.isEmpty())
		{
			AddressType addressType = AddressType.newInstance();
			addressType.setName("Adresse");
			this.merge(addressType);
			addressTypes.add(addressType);
		}
		return addressTypes;
	}

}
