/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.persistence.queries.SchoolLevelQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class SchoolLevelContentProvider implements IStructuredContentProvider
{
	@Override
	public Object[] getElements(Object object)
	{
		if (object instanceof ConnectionService)
		{
			ConnectionService service = (ConnectionService) object;
			SchoolLevelQuery query = (SchoolLevelQuery) service.getQuery(SchoolLevel.class);
			return query.selectAll(SchoolLevel.class).toArray(new SchoolLevel[0]);
		}
		return new SchoolLevel[0];
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
