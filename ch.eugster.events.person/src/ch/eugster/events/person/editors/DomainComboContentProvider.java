package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DomainComboContentProvider extends ArrayContentProvider
{
	private final boolean mandatoryDomain;

	public DomainComboContentProvider(final boolean mandatory)
	{
		this.mandatoryDomain = mandatory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(final Object inputElement)
	{
		if (inputElement instanceof ConnectionService)
		{
			ConnectionService connectionService = (ConnectionService) inputElement;
			DomainQuery query = (DomainQuery) connectionService.getQuery(Domain.class);
			Collection<Domain> domains = new ArrayList<Domain>();
			if (this.mandatoryDomain)
			{
				domains.add(Domain.newInstance());
			}
			domains.addAll(query.selectAll());
			return domains.toArray(new Domain[0]);
		}
		else if (inputElement instanceof Domain[])
		{
			if (this.mandatoryDomain)
			{
				return (Domain[]) inputElement;
			}
			else
			{
				Domain[] domains = (Domain[]) inputElement;
				Domain[] allDomains = new Domain[domains.length + 1];
				allDomains[0] = Domain.newInstance();
				for (int i = 0; i < domains.length; i++)
				{
					allDomains[i + 1] = domains[i];
				}
				return allDomains;
			}
		}
		else if (inputElement instanceof Collection<?>)
		{
			if (this.mandatoryDomain)
			{
				return ((Collection<Domain>) inputElement).toArray(new Domain[0]);
			}
			else
			{
				Collection<Domain> domains = (Collection<Domain>) inputElement;
				Collection<Domain> withEmpty = new ArrayList<Domain>();
				withEmpty.add(Domain.newInstance());
				withEmpty.addAll(domains);
				return withEmpty.toArray(new Domain[0]);
			}
		}
		return new Domain[0];
	}

}
