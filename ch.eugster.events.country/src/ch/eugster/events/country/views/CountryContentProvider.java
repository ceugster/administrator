/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.country.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CountryContentProvider implements IStructuredContentProvider
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
			CountryQuery query = (CountryQuery) connectionService.getQuery(Country.class);
			return query.selectAll().toArray(new Country[0]);
		}
		return new Country[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
