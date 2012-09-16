package ch.eugster.events.course.editors;

import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Category;

public class CategoryContentProvider extends ArrayContentProvider
{
	private boolean mandatoryCategory;
	public CategoryContentProvider(boolean mandatory)
	{
		mandatoryCategory = mandatory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement)
	{
		if (mandatoryCategory)
		{
			if (inputElement instanceof Category[])
			{
				return (Category[]) inputElement;
			}
			else if (inputElement instanceof Collection<?>)
			{
				return ((Collection<Category>) inputElement).toArray(new Category[0]);
			}
		}
		else
			if (inputElement instanceof Category[])
			{
				Category[] list = (Category[]) inputElement;
				Category[] categories = new Category[list.length + 1];
				for (int i = 0; i < list.length; i++)
					categories[i + 1] = list[i];
				categories[0] = Category.newInstance();
			}
			else if (inputElement instanceof Collection<?>)
			{
				Category[] list = ((Collection<Category>) inputElement).toArray(new Category[0]);
				Category[] categories = new Category[list.length + 1];
				for (int i = 0; i < list.length; i++)
					categories[i + 1] = list[i];
				categories[0] = Category.newInstance();
			}
		return new Category[0];
	}

}
