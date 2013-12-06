package ch.eugster.events.member.dialog;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Membership;

public class MemberTableContentProvider implements IStructuredContentProvider
{
	@Override
	public Object[] getElements(Object object)
	{
		if (object instanceof Membership[])
		{
			return (Membership[]) object;
		}
		return new Membership[0];
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	@Override
	public void dispose()
	{
	}

}
