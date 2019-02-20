package ch.eugster.events.user.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.User;
import ch.eugster.events.user.Activator;

public class StateLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof User.UserStatus)
		{
			User.UserStatus status = (User.UserStatus) element;
			if (status.equals(User.UserStatus.ADMINISTRATOR))
				return Activator.getDefault().getImageRegistry().get("ADMINISTRATOR");
			if (status.equals(User.UserStatus.ADMINISTRATOR))
				return Activator.getDefault().getImageRegistry().get("MANAGER");
			if (status.equals(User.UserStatus.ADMINISTRATOR))
				return Activator.getDefault().getImageRegistry().get("USER");
		}
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof User.UserStatus)
		{
			User.UserStatus status = (User.UserStatus) element;
			return status.toString();
		}
		return "";
	}

}
