package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.service.ConnectionService;

public class FieldExtensionQuery extends AbstractEntityQuery<FieldExtension>
{
	public FieldExtensionQuery(final ConnectionService service)
	{
		super(service);
	}

	public List<FieldExtension> selectAll()
	{
		return super.selectAll(FieldExtension.class, true);
	}

	public List<FieldExtension> selectAll(final boolean deletedToo)
	{
		return super.selectAll(FieldExtension.class, deletedToo);
	}

	public List<FieldExtension> selectByTarget(final FieldExtensionTarget target, final boolean deletedToo)
	{
		if (target == null)
		{
			return new ArrayList<FieldExtension>();
		}

		Expression expression = new ExpressionBuilder(FieldExtension.class).get("target").equal(target);
		if (!deletedToo)
		{
			Expression deleted = new ExpressionBuilder().get("deleted").equal(false);
			expression = expression.and(deleted);
		}
		return select(FieldExtension.class, expression);
	}

	public List<FieldExtension> selectSearchables(final boolean deletedToo)
	{
		Expression expression = new ExpressionBuilder(FieldExtension.class).get("searchable").equal(true);
		if (!deletedToo)
		{
			Expression deleted = new ExpressionBuilder().get("deleted").equal(false);
			expression = expression.and(deleted);
		}
		return select(FieldExtension.class, expression);
	}

}
