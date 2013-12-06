/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.rubric.views;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Rubric;
import ch.eugster.events.persistence.queries.RubricQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class RubricContentProvider implements IStructuredContentProvider
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
			RubricQuery query = (RubricQuery) service.getQuery(Rubric.class);
			Collection<Rubric> rubrics = query.selectAll();
			return rubrics.toArray(new Rubric[0]);
		}
		return new Rubric[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
