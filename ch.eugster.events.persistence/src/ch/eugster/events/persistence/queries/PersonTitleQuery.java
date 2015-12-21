package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;

import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonTitleQuery extends AbstractEntityQuery<PersonTitle>
{

	public PersonTitleQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<PersonTitle> selectAll()
	{
		return super.selectAll(PersonTitle.class, true);
	}

	public List<PersonTitle> selectByTitle(String title)
	{
		Expression expression = new ExpressionBuilder(PersonTitle.class).get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("title").equal(title));
		return this.select(PersonTitle.class, expression);
	}
	
	public List<PersonTitle> selectAll(final boolean deletedToo)
	{
		return super.selectAll(PersonTitle.class, deletedToo);
	}

	@SuppressWarnings("unchecked")
	public List<PersonTitle> selectMatching(final String input)
	{
		Expression expression = new ExpressionBuilder(PersonTitle.class);
		expression = expression.get("title").likeIgnoreCase(input + "%");
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		ReadAllQuery databaseQuery = new ReadAllQuery(PersonTitle.class, expression);
		Expression order = new ExpressionBuilder().get("title").ascending();
		List<Expression> orders = new ArrayList<Expression>();
		orders.add(order);
		databaseQuery.setOrderByExpressions(orders);
		return JpaHelper.createQuery(databaseQuery, this.connectionService.getEntityManager()).getResultList();
	}
}
