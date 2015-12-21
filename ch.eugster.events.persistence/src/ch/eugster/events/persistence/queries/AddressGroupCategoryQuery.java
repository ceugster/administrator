package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupCategoryQuery extends AbstractEntityQuery<AddressGroupCategory>
{

	public AddressGroupCategoryQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public long countByDomain(Domain domain)
	{
		Expression expression = new ExpressionBuilder(AddressGroupCategory.class).get("domain").equal(domain);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return count(AddressGroupCategory.class, expression);
	}

	public List<AddressGroupCategory> selectByDomain(Domain domain)
	{
		Expression expression = new ExpressionBuilder(AddressGroupCategory.class).get("domain").equal(domain);
		return select(AddressGroupCategory.class, expression);
	}

	public boolean isCodeUnique(String code, Long id)
	{
		if (code == null || code.length() == 0)
			return true;
		Expression expression = new ExpressionBuilder(AddressGroupCategory.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<AddressGroupCategory> categories = select(AddressGroupCategory.class, expression);
		if (categories.isEmpty())
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
				for (AddressGroupCategory category : categories)
				{
					if (!category.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

}
