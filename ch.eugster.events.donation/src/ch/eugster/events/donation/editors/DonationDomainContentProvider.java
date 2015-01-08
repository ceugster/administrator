/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.donation.editors;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationDomainContentProvider implements IStructuredContentProvider
{
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object object)
	{
		if (object instanceof ConnectionService)
		{
			ConnectionService connectionService = (ConnectionService) object;
			DomainQuery query = (DomainQuery) connectionService.getQuery(Domain.class);
			List<Domain> domains = query.selectValids();
			return domains.toArray(new Domain[0]);
		}
		return new Domain[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
