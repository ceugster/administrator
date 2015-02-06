package ch.eugster.events.persistence.queries;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonQuery extends AbstractEntityQuery<Person>
{

	public PersonQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public long countByCountry(final Country country)
	{
		Expression expression = new ExpressionBuilder(Person.class).get("country").equal(country);
		return this.count(Person.class, expression);
	}

	public long countByFirstnameAndLastnameAsLike(final String firstname, final String lastname, final Long id)
	{
		Expression expression = new ExpressionBuilder(Person.class).get("firstname").likeIgnoreCase(firstname + "%");
		expression = expression.and(new ExpressionBuilder().get("lastname").likeIgnoreCase(lastname + "%"));
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		if (id != null)
		{
			expression = expression.and(new ExpressionBuilder().get("id").notEqual(id));
		}
		return this.count(Person.class, expression);
	}

	public long countBySex(final PersonSex personSex)
	{
		Expression expression = new ExpressionBuilder(Person.class).get("sex").equal(personSex);
		return this.count(Person.class, expression);
	}

	public long countByTitle(final PersonTitle personTitle)
	{
		Expression expression = new ExpressionBuilder(Person.class).get("title").equal(personTitle);
		return this.count(Person.class, expression);
	}

	private Expression createCriteriaExpression(final Map<String, String> criteria, Expression expression)
	{
		Expression lastname = null, firstname = null, street = null, city = null, phone = null, email = null;
		Set<Entry<String, String>> entries = criteria.entrySet();
		for (Entry<String, String> entry : entries)
		{
			if (entry.getKey().equals("lastname"))
			{
				lastname = getLastnameExpression(entry.getValue());
			}
			else if (entry.getKey().equals("firstname"))
			{
				firstname = getFirstnameExpression(entry.getValue());
			}
			else if (entry.getKey().equals("organization"))
			{
				firstname = getOrganizationExpression(entry.getValue());
			}
			else if (entry.getKey().equals("address"))
			{
				street = getAddressExpression(entry.getValue());
			}
			else if (entry.getKey().equals("city"))
			{
				city = getCityExpression(entry.getValue());
			}
			else if (entry.getKey().equals("phone"))
			{
				phone = getPhoneExpression(entry.getValue());
			}
			else if (entry.getKey().equals("email"))
			{
				email = getEmailExpression(entry.getValue());
			}
		}
		Expression name = null, address = null, contacts = null;
		if (lastname != null)
		{
			if (firstname != null)
			{
				name = lastname.and(firstname);
			}
			else
			{
				name = lastname;
			}
		}
		else if (firstname != null)
		{
			name = firstname;
		}

		if (street != null)
		{
			if (city != null)
			{
				address = street.and(city);
			}
			else
			{
				address = street;
			}
		}
		else if (city != null)
		{
			address = city;
		}

		if (phone != null)
		{
			if (email != null)
			{
				contacts = phone.and(email);
			}
			else
			{
				contacts = phone;
			}
		}
		else if (email != null)
		{
			contacts = email;
		}

		Expression complex = null;
		if (name != null)
		{
			if (address != null)
			{
				complex = name.and(address);
			}
			else
			{
				complex = name;
			}
		}
		else if (address != null)
		{
			complex = address;
		}

		if (contacts != null)
		{
			if (complex != null)
			{
				expression = contacts.and(complex);
			}
			else
			{
				expression = contacts;
			}
		}
		else if (complex != null)
		{
			expression = complex;
		}
		return expression;
	}

	private Expression getAddressExpression(final String value)
	{
		return new ExpressionBuilder().anyOf("links").get("address").get("address")
				.containsSubstringIgnoringCase(value);
	}

	private Expression getCityExpression(final String value)
	{
		Expression city = new ExpressionBuilder().anyOf("links").get("address").get("zip")
				.containsSubstringIgnoringCase(value);
		city.or(new ExpressionBuilder().anyOf("links").get("address").get("city").containsSubstringIgnoringCase(value));
		return city;
	}

	private Expression getEmailExpression(final String value)
	{
		Expression email = new ExpressionBuilder().get("email").containsSubstringIgnoringCase(value);
		email.or(new ExpressionBuilder().anyOf("links").get("email").containsSubstringIgnoringCase(value));
		return email;
	}

	private Expression getFirstnameExpression(final String value)
	{
		return new ExpressionBuilder().get("firstname").containsSubstringIgnoringCase(value);
	}

	private Expression getLastnameExpression(final String value)
	{
		return new ExpressionBuilder().get("lastname").containsSubstringIgnoringCase(value);
	}

	private Expression getOrganizationExpression(final String value)
	{
		return new ExpressionBuilder().anyOf("links").get("address").get("name").containsSubstringIgnoringCase(value);
	}

	private Expression getPhoneExpression(final String value)
	{
		Expression phone = new ExpressionBuilder().get("phone").containsSubstringIgnoringCase(value);
		phone.or(new ExpressionBuilder().anyOf("links").get("phone").containsSubstringIgnoringCase(value));
		phone.or(new ExpressionBuilder().anyOf("links").get("address").get("phone")
				.containsSubstringIgnoringCase(value));
		phone.or(new ExpressionBuilder().anyOf("links").get("address").get("fax").containsSubstringIgnoringCase(value));
		return phone;
	}

	public List<Person> selectAll(final boolean deletedToo)
	{
		return super.selectAll(Person.class, deletedToo);
	}

	public List<Person> selectByCriteria(final Map<String, String> criteria)
	{
		return selectByCriteria(criteria, 0);
	}

	public List<Person> selectByCriteria(final Map<String, String> criteria, final int maxResults)
	{
		Expression expression = createCriteriaExpression(criteria, new ExpressionBuilder(Person.class));
		return select(Person.class, expression, maxResults);
	}

}
