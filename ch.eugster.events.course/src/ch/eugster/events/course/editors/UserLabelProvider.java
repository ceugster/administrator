package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.User;

public class UserLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof User)
		{
			User user = (User) element;
			if (!user.getFullname().isEmpty())
			{
				return user.getFullname();
			}
			if (!user.getUsername().isEmpty())
			{
				return user.getUsername();
			}
		}
		return "";
	}

}
