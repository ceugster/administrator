package ch.eugster.events.member.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Person;

public class MemberContentProvider implements IStructuredContentProvider
{
	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof Person)
		{
			return ((Person) inputElement).getMembers().toArray(new Member[0]);
		}
		else if (inputElement instanceof LinkPersonAddress)
		{
			return ((LinkPersonAddress) inputElement).getMembers().toArray(new Member[0]);
		}
		else if (inputElement instanceof Member[])
		{
			return (Member[]) inputElement;
		}
		return new Member[0];
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
