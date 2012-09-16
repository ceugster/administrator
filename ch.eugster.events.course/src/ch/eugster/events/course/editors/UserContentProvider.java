package ch.eugster.events.course.editors;

import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.User;

public class UserContentProvider extends ArrayContentProvider
{
	private boolean mandatoryUser;
	public UserContentProvider(boolean mandatory)
	{
		this.mandatoryUser = mandatory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement)
	{
		User[] users = new User[0];
		if (this.mandatoryUser)
		{
			if (inputElement instanceof User[])
			{
				users = (User[]) inputElement;
			}
			else if (inputElement instanceof Collection<?>)
			{
				users = ((Collection<User>) inputElement).toArray(new User[0]);
			}
		}
		else
		{
			if (inputElement instanceof User[])
			{
				User[] list = (User[]) inputElement;
				users = new User[list.length + 1];
				for (int i = 0; i < list.length; i++)
					users[i + 1] = list[i];
			}
			else if (inputElement instanceof Collection<?>)
			{
				User[] list = ((Collection<User>) inputElement).toArray(new User[0]);
				users = new User[list.length + 1];
				for (int i = 0; i < list.length; i++)
					users[i + 1] = list[i];
			}
			users[0] = User.newInstance();
		}

		return users;
	}

}
