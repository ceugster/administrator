package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Category;

public class CategorySorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof Category)
		{
			if (e2 instanceof Category)
			{
				Category category1 = (Category) e1;
				Category category2 = (Category) e2;

				if (category1.getCode().equals(category2.getCode()))
					return category1.getName().compareTo(category2.getName());
				else
					return category1.getCode().compareTo(category2.getCode());
			}
		}
		return 0;
	}

}
