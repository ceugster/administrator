/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.guide.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.queries.CompensationTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CompensationTypeContentProvider implements IStructuredContentProvider
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
			ConnectionService con = (ConnectionService) object;
			CompensationTypeQuery query = (CompensationTypeQuery) con.getQuery(CompensationType.class);
			List<CompensationType> compensationTypes = query.selectAll();
			return compensationTypes.toArray(new CompensationType[0]);
		}
		return new CompensationType[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
