/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.addresstype.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressTypeContentProvider implements IStructuredContentProvider
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
			ConnectionService service = (ConnectionService) object;
			AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
			return query.selectAll().toArray(new AddressType[0]);
		}
		return new AddressType[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
