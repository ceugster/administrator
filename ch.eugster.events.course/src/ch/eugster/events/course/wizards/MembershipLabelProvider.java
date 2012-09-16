package ch.eugster.events.course.wizards;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.Membership;

public class MembershipLabelProvider extends LabelProvider implements IBaseLabelProvider
{

	@Override
	public String getText(Object element)
	{
		if (element instanceof Membership)
		{
			return ((Membership) element).format();
		}
		return "";
	}

}
