/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.persistence.queries.EmailAccountQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class EmailAccountContentProvider implements IStructuredContentProvider
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
			EmailAccountQuery query = (EmailAccountQuery) service.getQuery(EmailAccount.class);
			return query.selectAll(EmailAccount.class).toArray(new EmailAccount[0]);
		}
		return new EmailAccount[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
