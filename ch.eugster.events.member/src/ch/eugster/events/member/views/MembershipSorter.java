package ch.eugster.events.member.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Membership;

public class MembershipSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		Membership d1 = (Membership) e1;
		Membership d2 = (Membership) e2;

		return d1.getName().compareTo(d2.getName());
	}

}
