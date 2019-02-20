/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.persistence.queries.TeacherQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class TeacherTreeViewerContentProvider implements ITreeContentProvider
{
	@Override
	public Object[] getElements(Object object)
	{
		return getChildren(object);
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	@Override
	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof ConnectionService)
		{
			ConnectionService service = (ConnectionService) parentElement;
			TeacherQuery query = (TeacherQuery) service.getQuery(Teacher.class);
			return query.selectAll(Teacher.class).toArray(new Teacher[0]);
		}
		return new Teacher[0];
	}

	@Override
	public Object getParent(Object element)
	{
		if (element instanceof Teacher)
		{
			return null;
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element)
	{
		if (element instanceof Teacher)
		{
			return false;
		}
		return false;
	}
}
