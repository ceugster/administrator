package ch.eugster.events.persistence.queries;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.service.ConnectionService;

public class DomainQuery extends AbstractEntityQuery<Domain>
{
	public DomainQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(Domain.class).get("code").equal(code);
		List<Domain> domains = select(Domain.class, expression);
		if (domains.isEmpty())
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
				for (Domain domain : domains)
				{
					if (!domain.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public List<Domain> selectAll()
	{
		List<Domain> domains = super.selectAll(Domain.class);
		if (domains.isEmpty())
		{
			Domain domain = Domain.newInstance();
			domain.setName("Standard");
			this.merge(domain);
			domains.add(domain);
		}
		return domains;
	}

	public List<Domain> selectValids()
	{
		Expression expression = new ExpressionBuilder(Domain.class).get("deleted").equal(false);
		List<Domain> domains = select(Domain.class, expression);
		if (domains.isEmpty())
		{
			Domain domain = Domain.newInstance();
			domain.setName("Standard");
			this.merge(domain);
			domains.add(domain);
		}
		return domains;
	}

	public List<Domain> selectValidsAndEmpty()
	{
		Expression expression = new ExpressionBuilder(Domain.class).get("deleted").equal(false);
		List<Domain> domains = select(Domain.class, expression);
		if (domains.isEmpty())
		{
			Domain domain = Domain.newInstance();
			domain.setName("Standard");
			this.merge(domain);
			domains.add(domain);
		}
		domains.add(Domain.newInstance());
		return domains;
	}

	public List<Domain> selectValidsAndAllAndEmpty()
	{
		Expression expression = new ExpressionBuilder(Domain.class).get("deleted").equal(false);
		List<Domain> domains = select(Domain.class, expression);
		if (domains.isEmpty())
		{
			Domain domain = Domain.newInstance();
			domain.setName("Standard");
			this.merge(domain);
			domains.add(domain);
		}
		domains.add(0, Domain.newInstance("Alle"));
		domains.add(0, Domain.newInstance("Keine"));
		return domains;
	}

	public Domain selectByCode(final String code)
	{
		Expression expression = new ExpressionBuilder(Domain.class).get("code").equal(code);
		List<Domain> domains = select(Domain.class, expression);
		try
		{
			return domains.iterator().next();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}
}
