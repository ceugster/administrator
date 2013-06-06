package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressQuery extends AbstractEntityQuery<Address>
{

	public AddressQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public long countByCountry(final Country country)
	{
		Expression expression = new ExpressionBuilder(Address.class).get("country").equal(country);
		return this.count(Address.class, expression);
	}

	public long countBySalutation(final AddressSalutation salutation)
	{
		Expression expression = new ExpressionBuilder(Address.class).get("salutation").equal(salutation);
		return this.count(Address.class, expression);
	}

	private Expression createCriteriaExpression(final Map<String, String> criteria)
	{
		Expression organisation = null, street = null, city = null, phone = null, email = null;
		Set<Entry<String, String>> entries = criteria.entrySet();
		for (Entry<String, String> entry : entries)
		{
			if (entry.getKey().equals("organisation"))
			{
				organisation = getOrganisationExpression(entry.getValue());
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

		Expression whole = this.getNoLinksExpression();

		if (organisation != null)
		{
			whole = whole.and(organisation);
		}
		if (street != null)
		{
			if (whole == null)
			{
				whole = street;
			}
			else
			{
				whole = whole.and(street);
			}
		}
		if (city != null)
		{
			if (whole == null)
			{
				whole = city;
			}
			else
			{
				whole = whole.and(city);
			}
		}
		if (phone != null)
		{
			if (whole == null)
			{
				whole = phone;
			}
			else
			{
				whole = whole.and(phone);
			}
		}
		if (email != null)
		{
			if (whole == null)
			{
				whole = email;
			}
			else
			{
				whole = whole.and(email);
			}
		}
		return whole;
	}

	private Expression getAddressExpression(final String value)
	{
		return new ExpressionBuilder().get("address").containsSubstringIgnoringCase(value);
	}

	private Expression getCityExpression(final String value)
	{
		Expression city = new ExpressionBuilder().get("zip").containsSubstringIgnoringCase(value);
		city.or(new ExpressionBuilder().get("city").containsSubstringIgnoringCase(value));
		return city;
	}

	private Expression getEmailExpression(final String value)
	{
		return new ExpressionBuilder().get("email").containsSubstringIgnoringCase(value);
	}

	private Expression getNoLinksExpression()
	{
		return new ExpressionBuilder(Address.class).isEmpty("links");
	}

	private Expression getOrganisationExpression(final String value)
	{
		return new ExpressionBuilder().get("name").containsSubstringIgnoringCase(value);
	}

	private Expression getPhoneExpression(final String value)
	{
		Expression phone = new ExpressionBuilder().get("phone").containsSubstringIgnoringCase(value);
		phone.or(new ExpressionBuilder().get("fax").containsSubstringIgnoringCase(value));
		return phone;
	}

	public Collection<Address> selectAddresses(final Map<String, String> criteria)
	{

		if (criteria.size() == 0)
		{
			return new ArrayList<Address>();
		}

		String[] keys = criteria.keySet().toArray(new String[0]);
		String firstKey = keys[0];
		Expression expression = new ExpressionBuilder(Address.class).get(firstKey).containsSubstringIgnoringCase(
				criteria.get(firstKey));
		for (String key : keys)
		{
			if (!key.equals(firstKey))
				expression = expression.and(new ExpressionBuilder().get(key).containsSubstringIgnoringCase(
						criteria.get(key)));
		}

		return select(Address.class, expression);
	}

	public Collection<Address> selectByCriteria(final Map<String, String> criteria, final int maxResults)
	{
		Expression expression = createCriteriaExpression(criteria);
		if (expression != null)
		{
			return select(Address.class, expression, maxResults);
		}
		else
		{
			return new ArrayList<Address>();
		}
	}

}
