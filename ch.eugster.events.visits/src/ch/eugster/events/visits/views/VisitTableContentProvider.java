package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class VisitTableContentProvider implements IStructuredContentProvider
{

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof ConnectionService)
		{
			ConnectionService service = (ConnectionService) inputElement;
			VisitQuery query = (VisitQuery) service.getQuery(Visit.class);
			return query.selectAfterLastYear().toArray(new Visit[0]);
		}
		return new Object[0];
	}
}
