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

	public Collection<Address> selectByAddressAsLike(final String address)
	{
		Collection<Address> addresses = new ArrayList<Address>();
		Expression expression = new ExpressionBuilder(Address.class).get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("address").likeIgnoreCase(address + "%"));
		Collection<Address> selected = this.select(Address.class, expression);
		for (Address select : selected)
		{
			if (select.getPersonLinks() == null || select.getPersonLinks().isEmpty())
			{
				addresses.add(select);
			}
		}
		return addresses;
	}

	public Collection<Address> selectWithEmptyProvince()
	{
		Expression expression = new ExpressionBuilder(Address.class).get("deleted").equal(false);
		expression.and(new ExpressionBuilder().get("country").notNull());
		Expression empty = new ExpressionBuilder().get("province").isNull().or(new ExpressionBuilder().get("province").equal(""));
		expression = expression.and(empty);
		Collection<Address> selected = this.select(Address.class, expression);
		return selected;
	}

	private Expression createCriteriaExpression(final Map<String, String> criteria)
	{
		Expression organization = null, street = null, city = null, phone = null, email = null;
		Set<Entry<String, String>> entries = criteria.entrySet();
		for (Entry<String, String> entry : entries)
		{
			if (entry.getKey().equals("organization"))
			{
				organization = getOrganizationExpression(entry.getValue());
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

		if (organization != null)
		{
			if (whole == null)
			{
				whole = organization;
			}
			else
			{
				whole = whole.and(organization);
			}
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
		city = city.or(new ExpressionBuilder().get("city").containsSubstringIgnoringCase(value));
		return city;
	}

	private Expression getEmailExpression(final String value)
	{
		return new ExpressionBuilder().get("email").containsSubstringIgnoringCase(value);
	}

	private Expression getNoLinksExpression()
	{
		// return new ExpressionBuilder(Address.class).isEmpty("links");
		return null;
	}

	private Expression getOrganizationExpression(final String value)
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
