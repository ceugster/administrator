package ch.eugster.events.category.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Category;

public class CategorySorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		Category category1 = (Category) e1;
		Category category2 = (Category) e2;

		if (category1.getCode().equals(""))
		{
			if (category2.getCode().equals(""))
				return category1.getName().compareTo(category2.getName());
			else
				return -1;
		}
		else
		{
			if (category2.getCode().equals(""))
				return 1;
			else
				return category1.getCode().compareTo(category2.getCode());
		}
	}
}
