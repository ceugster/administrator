package ch.eugster.events.persistence.queries;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.User.UserStatus;
import ch.eugster.events.persistence.service.ConnectionService;

public class UserQuery extends AbstractEntityQuery<User>
{

	public UserQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public long count(final UserStatus status)
	{
		Expression expression = new ExpressionBuilder(User.class).get("state").equal(status);
		return count(User.class, expression);
	}

	public boolean isUsernameUnique(final String username, final Long id)
	{
		Expression expression = new ExpressionBuilder(User.class).get("username").equal(username);
		Collection<User> users = select(User.class, expression);
		if (users.isEmpty())
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
				for (User user : users)
				{
					if (!user.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public Collection<User> selectAll()
	{
		return super.selectAll(User.class);
	}

	public User selectByUsername(final String username)
	{
		Expression expression = new ExpressionBuilder(User.class).get("username").equal(username);
		Collection<User> users = select(User.class, expression);
		try
		{
			return users.iterator().next();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}

	public User selectByUsernameAndPassword(final String username, final String password)
	{
		Expression exp1 = new ExpressionBuilder(User.class).get("username").equal(username);
		Expression exp2 = new ExpressionBuilder(User.class).get("password").equal(password);
		Expression exp = exp1.and(exp2);
		return (User) this.select(User.class, exp);
	}

}
