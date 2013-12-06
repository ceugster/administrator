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
		StringBuilder builder = new StringBuilder("");
		if (element instanceof User)
		{
			User user = (User) element;
			if (!user.getUsername().isEmpty())
			{
				builder = builder.append(user.getUsername());
				if (!user.getFullname().isEmpty())
					builder = builder.append(" - ");
			}
			if (!user.getFullname().isEmpty())
				builder = builder.append(user.getFullname());
		}
		return builder.toString();
	}

}
