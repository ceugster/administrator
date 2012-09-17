/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.person.views;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.queries.AddressSalutationQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressSalutationContentProvider implements IStructuredContentProvider
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
			AddressSalutationQuery query = (AddressSalutationQuery) service.getQuery(AddressSalutation.class);
			Collection<AddressSalutation> titles = query.selectAll();
			return titles.toArray(new AddressSalutation[0]);
		}
		else if (object instanceof AddressSalutation[])
		{
			return (AddressSalutation[]) object;
		}

		return new AddressSalutation[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}

}
