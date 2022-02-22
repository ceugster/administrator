package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.Rubric;

public class RubricLabelProvider extends LabelProvider
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
		if (element instanceof Rubric)
		{
			Rubric rubric = (Rubric) element;
			if (!rubric.getCode().isEmpty())
			{
				builder = builder.append(rubric.getCode());
				if (!rubric.getName().isEmpty())
					builder = builder.append(" - ");
			}
			if (!rubric.getName().isEmpty())
				builder = builder.append(rubric.getName());
		}
		return builder.toString();
	}

}
