package ch.eugster.events.course.editors;

import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Category;

public class CategoryContentProvider extends ArrayContentProvider
{
	private boolean mandatoryCategory;

	public CategoryContentProvider(final boolean mandatory)
	{
		mandatoryCategory = mandatory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(final Object inputElement)
	{
		Category[] categories = new Category[0];
		if (mandatoryCategory)
		{
			if (inputElement instanceof Category[])
			{
				categories = (Category[]) inputElement;
			}
			else if (inputElement instanceof Collection<?>)
			{
				categories = ((Collection<Category>) inputElement).toArray(new Category[0]);
			}
		}
		else if (inputElement instanceof Category[])
		{
			Category[] list = (Category[]) inputElement;
			categories = new Category[list.length + 1];
			for (int i = 0; i < list.length; i++)
				categories[i + 1] = list[i];
			categories[0] = Category.newInstance();
		}
		else if (inputElement instanceof Collection<?>)
		{
			Category[] list = ((Collection<Category>) inputElement).toArray(new Category[0]);
			categories = new Category[list.length + 1];
			for (int i = 0; i < list.length; i++)
				categories[i + 1] = list[i];
			categories[0] = Category.newInstance();
		}
		return categories;
	}

}
