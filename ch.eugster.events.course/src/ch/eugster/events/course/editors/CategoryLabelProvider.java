package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.Category;

public class CategoryLabelProvider extends LabelProvider
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
		if (element instanceof Category)
		{
			Category category = (Category) element;
			if (!category.getCode().isEmpty())
			{
				builder = builder.append(category.getCode());
				if (!category.getName().isEmpty())
					builder = builder.append(" - ");
			}
			if (!category.getName().isEmpty())
				builder = builder.append(category.getName());
		}
		return builder.toString();
	}

}
