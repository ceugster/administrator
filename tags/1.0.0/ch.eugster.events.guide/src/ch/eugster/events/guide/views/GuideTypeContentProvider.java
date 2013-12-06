/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.guide.views;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.persistence.queries.GuideTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class GuideTypeContentProvider implements IStructuredContentProvider
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
			GuideTypeQuery query = (GuideTypeQuery) con.getQuery(GuideType.class);
			Collection<GuideType> guideTypes = query.selectAll();
			return guideTypes.toArray(new GuideType[0]);
		}
		return new GuideType[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
