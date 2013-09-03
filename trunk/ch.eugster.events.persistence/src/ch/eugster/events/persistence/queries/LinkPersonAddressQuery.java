package ch.eugster.events.persistence.queries;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.service.ConnectionService;

public class LinkPersonAddressQuery extends AbstractEntityQuery<LinkPersonAddress>
{

	public LinkPersonAddressQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	private Expression createCriteriaExpression(final Map<String, String> criteria,
			final Map<String, FieldExtension> extensions, Expression expression)
	{
		Expression lastname = null, firstname = null, organization = null, street = null, city = null, phone = null, email = null, extended = null;
		Set<Entry<String, String>> entries = criteria.entrySet();
		for (Entry<String, String> entry : entries)
		{
			if (entry.getKey().equals("lastname"))
			{
				expression = expression.and(getLastnameExpression(entry.getValue()));
			}
			else if (entry.getKey().equals("firstname"))
			{
				expression = expression.and(getFirstnameExpression(entry.getValue()));
			}
			else if (entry.getKey().equals("organization"))
			{
				expression = expression.and(getOrganizationExpression(entry.getValue()));
			}
			else if (entry.getKey().equals("address"))
			{
				expression = expression.and(getAddressExpression(entry.getValue()));
			}
			else if (entry.getKey().equals("city"))
			{
				expression = expression.and(getCityExpression(entry.getValue()));
			}
			else if (entry.getKey().equals("phone"))
			{
				expression = expression.and(getPhoneExpression(entry.getValue()));
			}
			else if (entry.getKey().equals("email"))
			{
				expression = expression.and(getEmailExpression(entry.getValue()));
			}
			else
			{
				FieldExtension extension = extensions.get(entry.getKey());
				if (extension != null)
				{
					if (extended == null)
					{
						extended = getExtendedExpression(extension, entry.getValue());
					}
					else
					{
						Expression expr = getExtendedExpression(extension, entry.getValue());
						if (expr != null)
						{
							extended.and(expr);
						}
					}
				}
			}
		}
		// Expression name = null, address = null, contacts = null;
		// if (lastname != null)
		// {
		// if (firstname != null)
		// {
		// name = lastname.and(firstname);
		// }
		// else
		// {
		// name = lastname;
		// }
		// }
		// else if (firstname != null)
		// {
		// name = firstname;
		// }
		//
		// if (street != null)
		// {
		// if (city != null)
		// {
		// address = street.and(city);
		// }
		// else
		// {
		// address = street;
		// }
		// }
		// else if (city != null)
		// {
		// address = city;
		// }
		//
		// if (phone != null)
		// {
		// if (email != null)
		// {
		// contacts = phone.and(email);
		// }
		// else
		// {
		// contacts = phone;
		// }
		// }
		// else if (email != null)
		// {
		// contacts = email;
		// }
		//
		// Expression complex = null;
		// if (name != null)
		// {
		// if (address != null)
		// {
		// complex = name.and(address);
		// }
		// else
		// {
		// complex = name;
		// }
		// }
		// else if (address != null)
		// {
		// complex = address;
		// }
		//
		// if (contacts != null)
		// {
		// if (complex != null)
		// {
		// expression = contacts.and(complex);
		// }
		// else
		// {
		// expression = contacts;
		// }
		// }
		//
		// if (contacts != null)
		// {
		// if (complex != null)
		// {
		// expression = contacts.and(complex);
		// }
		// else
		// {
		// expression = contacts;
		// }
		// }
		// else if (complex != null)
		// {
		// expression = complex;
		// }
		//
		// if (extended != null)
		// {
		// expression = expression.and(extended);
		// }
		return expression;
	}

	private Expression getAddressExpression(final String value)
	{
		return new ExpressionBuilder().get("address").get("address").containsSubstringIgnoringCase(value);
	}

	private Expression getCityExpression(final String value)
	{
		Expression city = null;
		try
		{
			Integer.valueOf(value);
			city = new ExpressionBuilder().get("address").get("zip").containsSubstringIgnoringCase(value);
		}
		catch (NumberFormatException e)
		{
			city = (new ExpressionBuilder().get("address").get("city").containsSubstringIgnoringCase(value));
		}
		return city;
	}

	private Expression getEmailExpression(final String value)
	{
		Expression email = new ExpressionBuilder(LinkPersonAddress.class).get("email")
				.containsSubstringIgnoringCase(value)
				.or(new ExpressionBuilder().get("address").get("email").containsSubstringIgnoringCase(value))
				.or(new ExpressionBuilder().get("person").get("email").containsSubstringIgnoringCase(value));
		return email;
	}

	private Expression getExtendedExpression(final FieldExtension extension, final String value)
	{
		if (extension.getTarget().equals(FieldExtensionTarget.PA_LINK))
		{
			return new ExpressionBuilder().anyOf("extendedFields").get("fieldExtension").get("id")
					.equal(extension.getId())
					.and(new ExpressionBuilder().anyOf("extendedFields").get("value").equal(value));
		}
		else if (extension.getTarget().equals(FieldExtensionTarget.PERSON))
		{
			return new ExpressionBuilder().get("person").anyOf("extendedFields").get("fieldExtension").get("id")
					.equal(extension.getId())
					.and(new ExpressionBuilder().get("person").anyOf("extendedFields").get("value").equal(value));
		}
		return null;
	}

	private Expression getFirstnameExpression(final String value)
	{
		return new ExpressionBuilder().get("person").get("firstname").containsSubstringIgnoringCase(value);
	}

	private Expression getLastnameExpression(final String value)
	{
		return new ExpressionBuilder().get("person").get("lastname").containsSubstringIgnoringCase(value);
	}

	private Expression getOrganizationExpression(final String value)
	{
		return new ExpressionBuilder().get("address").get("name").containsSubstringIgnoringCase(value);
	}

	private Expression getPhoneExpression(final String value)
	{
		Expression phone = new ExpressionBuilder(LinkPersonAddress.class).get("person").get("phone")
				.containsSubstringIgnoringCase(value)
				.or(new ExpressionBuilder().get("phone").containsSubstringIgnoringCase(value))
				.or(new ExpressionBuilder().get("address").get("phone").containsSubstringIgnoringCase(value))
				.or(new ExpressionBuilder().get("address").get("fax").containsSubstringIgnoringCase(value));
		return phone;
	}

	public Collection<LinkPersonAddress> selectByAddressAsLike(final String address)
	{
		Expression expression = new ExpressionBuilder(LinkPersonAddress.class).get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("address").get("deleted").equal(false));
		expression = expression
				.and(new ExpressionBuilder().get("address").get("address").likeIgnoreCase(address + "%"));
		return this.select(LinkPersonAddress.class, expression);
	}

	public Collection<LinkPersonAddress> selectByCriteria(final Map<String, String> criteria,
			final Map<String, FieldExtension> extensions)
	{
		return selectByCriteria(criteria, extensions, 0);
	}

	public Collection<LinkPersonAddress> selectByCriteria(final Map<String, String> criteria,
			final Map<String, FieldExtension> extensions, final int maxResults)
	{
		Expression expression = createCriteriaExpression(criteria, extensions, new ExpressionBuilder(
				LinkPersonAddress.class));
		Collection<LinkPersonAddress> links = select(LinkPersonAddress.class, expression, maxResults);
		return links;
	}

}
