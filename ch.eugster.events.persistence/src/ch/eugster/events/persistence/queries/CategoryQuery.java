package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Category;
import ch.eugster.events.persistence.service.ConnectionService;

public class CategoryQuery extends AbstractEntityQuery<Category>
{

	public CategoryQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Category.class).get("code").equal(code);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		Collection<Category> categories = select(Category.class, expression);
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
				for (Category category : categories)
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

	public Collection<Category> selectAll()
	{
		return super.selectAll(Category.class);
	}

}
