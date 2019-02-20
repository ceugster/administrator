package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.ContactType;
import ch.eugster.events.persistence.model.IEntity;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.service.ConnectionService;

public class ContactTypeQuery extends AbstractEntityQuery<ContactType>
{
	public ContactTypeQuery(ConnectionService service)
	{
		super(service);
	}

	public List<ContactType> selectContactTypes()
	{
		Expression expression = new ExpressionBuilder(ContactType.class).get("deleted").equal(false);
		return this.select(ContactType.class, expression);
	}

	public List<ContactType> selectContactTypes(Class<? extends IEntity>[] ownerClasses)
	{
		Expression expression = null;
		for (Class<? extends IEntity> ownerClass : ownerClasses)
		{
			expression = addExpression(expression, ownerClass);
		}
		if (expression == null)
		{
			return new ArrayList<ContactType>();
		}
		return this.select(ContactType.class, expression);
	}

	private Expression addExpression(Expression expression, Class<? extends IEntity> ownerClass)
	{
		if (ownerClass.equals(Address.class))
		{
			expression = createExpression(expression, "address");
		}
		else if (ownerClass.equals(LinkPersonAddress.class))
		{
			expression = createExpression(expression, "link");
		}
		else if (ownerClass.equals(Person.class))
		{
			expression = createExpression(expression, "person");
		}
		return expression;
	}

	private Expression createExpression(Expression expression, String fieldName)
	{
		if (expression == null)
		{
			expression = new ExpressionBuilder().get(fieldName).equal(true);
		}
		else
		{
			expression = expression.and(new ExpressionBuilder().get(fieldName).equal(true));
		}
		return expression;
	}
}
