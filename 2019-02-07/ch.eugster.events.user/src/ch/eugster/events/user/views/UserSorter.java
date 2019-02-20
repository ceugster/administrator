package ch.eugster.events.user.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.User;

public class UserSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		User user1 = (User) e1;
		User user2 = (User) e2;

		return user1.getUsername().compareTo(user2.getUsername());
	}
}
