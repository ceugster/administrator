/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.persistence.queries.ApplianceQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class ApplianceContentProvider implements IStructuredContentProvider
{
	@Override
	public Object[] getElements(Object object)
	{
		if (object instanceof ConnectionService)
		{
			ConnectionService service = (ConnectionService) object;
			ApplianceQuery query = (ApplianceQuery) service.getQuery(Appliance.class);
			return query.selectAll(Appliance.class).toArray(new Appliance[0]);
		}
		return new Appliance[0];
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}
}
