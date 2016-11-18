package ch.eugster.events.persistence.queries;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
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
		Expression zip = new ExpressionBuilder().anyOf("links").get("address").get("zip")
				.containsSubstringIgnoringCase(value);
		Expression city = new ExpressionBuilder().anyOf("links").get("address").get("city")
				.containsSubstringIgnoringCase(value);
		return city.or(zip);
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

	public List<Person> selectByCriteria(final Map<String, String> criteria,
			final Map<String, FieldExtension> extensions, final int maxResults)
	{
		Expression expression = createCriteriaExpression(criteria, extensions, new ExpressionBuilder(
				Person.class));
		List<Person> persons = select(Person.class, expression, maxResults);
		return persons;
	}
	
	private Expression createCriteriaExpression(final Map<String, String> criteria,
			final Map<String, FieldExtension> extensions, Expression expression)
	{
		Expression extended = null;
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
		return expression;
	}

	private Expression getExtendedExpression(final FieldExtension extension, final String value)
	{
		if (extension.getTarget().equals(FieldExtensionTarget.PA_LINK))
		{
			return new ExpressionBuilder().anyOf("links").anyOf("extendedFields").get("fieldExtension").get("id")
					.equal(extension.getId())
					.and(new ExpressionBuilder().anyOf("extendedFields").get("value").equal(value));
		}
		else if (extension.getTarget().equals(FieldExtensionTarget.PERSON))
		{
			return new ExpressionBuilder().anyOf("extendedFields").get("fieldExtension").get("id")
					.equal(extension.getId())
					.and(new ExpressionBuilder().get("person").anyOf("extendedFields").get("value").equal(value));
		}
		return null;
	}

}
