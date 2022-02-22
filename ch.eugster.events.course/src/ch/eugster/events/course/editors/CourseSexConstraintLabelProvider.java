package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.CourseSexConstraint;

public class CourseSexConstraintLabelProvider extends LabelProvider
{

	@Override
	public String getText(Object element)
	{
		if (element instanceof CourseSexConstraint)
			return ((CourseSexConstraint) element).toString();
		return "";
	}

}
