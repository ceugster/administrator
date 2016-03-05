package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.CourseState;

public class StateLabelProvider extends LabelProvider
{

	@Override
	public String getText(Object element)
	{
		if (element instanceof CourseState)
		{
			return ((CourseState) element).toString();
		}
		return "";
	}

}
