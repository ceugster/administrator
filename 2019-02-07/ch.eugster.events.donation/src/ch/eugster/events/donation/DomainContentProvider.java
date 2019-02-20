package ch.eugster.events.donation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DomainContentProvider extends ArrayContentProvider
{
	private Domain[] entries;
	
	private List<Domain> allAndEmptyDomains;

	public DomainContentProvider(List<Domain> allAndEmptyDomains)
	{
		this.allAndEmptyDomains = allAndEmptyDomains;
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof ConnectionService)
		{
			List<Domain> domains = new ArrayList<Domain>();
			domains.addAll(allAndEmptyDomains);
			ConnectionService connectionService = (ConnectionService) inputElement;
			DomainQuery query = (DomainQuery) connectionService.getQuery(Domain.class);
			domains.addAll(query.selectAll());
			this.entries = domains.toArray(new Domain[0]);
			return this.entries;
		}
		else if (inputElement instanceof Domain[])
		{
			this.entries = (Domain[])inputElement;
			return this.entries;
		}
		return new Domain[0];
	}
	
	public Domain[] getEntries()
	{
		return this.entries;
	}
}
