package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.User;

public class UserSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof User)
		{
			if (e2 instanceof User)
			{
				User user1 = (User) e1;
				User user2 = (User) e2;

				if (user1.getUsername().equals(user2.getUsername()))
					return user1.getFullname().compareTo(user2.getFullname());
				else
					return user1.getUsername().compareTo(user2.getUsername());
			}
		}
		return 0;
	}

}
