/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.SchoolClass;
import ch.eugster.events.persistence.queries.SchoolClassQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class SchoolClassContentProvider implements IStructuredContentProvider
{
	@Override
	public Object[] getElements(Object object)
	{
		if (object instanceof ConnectionService)
		{
			ConnectionService service = (ConnectionService) object;
			SchoolClassQuery query = (SchoolClassQuery) service.getQuery(SchoolClass.class);
			return query.selectAll(SchoolClass.class).toArray(new SchoolClass[0]);
		}
		return new SchoolClass[0];
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
