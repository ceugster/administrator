package ch.eugster.events.course.wizards;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Membership;

public class MembershipSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		Membership m1 = (Membership) e1;
		Membership m2 = (Membership) e2;
		return m1.format().compareTo(m2.format());
	}

}
