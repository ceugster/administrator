package ch.eugster.events.persistence.queries;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.service.ConnectionService;

public class CountryQuery extends AbstractEntityQuery<Country>
{

	public CountryQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public Country findByIso3166Alpha2Code(final String code)
	{
		Expression expression = new ExpressionBuilder().get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("iso3166alpha2").equal(code));
		Collection<Country> countries = select(Country.class, expression);
		try
		{
			return countries.iterator().next();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}

	public boolean isIso31662numericUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Country.class).get("iso31662numeric").equal(code);
		Collection<Country> countries = select(Country.class, expression);
		if (countries.isEmpty())
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
				for (Country country : countries)
				{
					if (!country.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public boolean isIso3166alpha2Unique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Country.class).get("iso3166alpha2").equal(code);
		Collection<Country> countries = select(Country.class, expression);
		if (countries.isEmpty())
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
				for (Country country : countries)
				{
					if (!country.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public boolean isIso3166alpha3Unique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Country.class).get("iso3166alpha3").equal(code);
		Collection<Country> countries = select(Country.class, expression);
		if (countries.isEmpty())
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
				for (Country country : countries)
				{
					if (!country.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public List<Country> selectAll()
	{
		return super.selectAll(Country.class);
	}

	public Collection<Country> selectByIso3166alpha2Code(final String code)
	{
		Expression expression = new ExpressionBuilder(Country.class).get("iso3166alpha2").equalsIgnoreCase(code);
		return select(Country.class, expression);
	}

	public Country selectDefault()
	{
		Locale locale = Locale.getDefault();
		return this.findByIso3166Alpha2Code(locale.getISO3Country());
	}

	public Collection<Country> selectPrefixes()
	{
		Expression expression = new ExpressionBuilder(Country.class).get("phonePrefix").notNull();
		expression = expression.and(new ExpressionBuilder().get("phonePrefix").notEqual(""));
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		expression = expression.and(new ExpressionBuilder().get("visible").equal(true));
		return select(Country.class, expression);
	}

	public Collection<Country> selectVisibles()
	{
		Expression expression = new ExpressionBuilder(Country.class).get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("visible").equal(true));
		return select(Country.class, expression);
	}
}
