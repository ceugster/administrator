package ch.eugster.events.member.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.queries.MembershipQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class MembershipContentProvider implements IStructuredContentProvider
{
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object inputElement)
	{
		if (inputElement instanceof ConnectionService)
		{
			ConnectionService service = (ConnectionService) inputElement;
			if (service != null)
			{
				MembershipQuery query = (MembershipQuery) service.getQuery(Membership.class);
				return query.selectAll().toArray(new Membership[0]);
			}
		}
		return new Membership[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
