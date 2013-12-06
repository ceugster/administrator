package ch.eugster.events.rubric.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Rubric;

public class RubricSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		Rubric rubric1 = (Rubric) e1;
		Rubric rubric2 = (Rubric) e2;

		if (rubric1.getCode().equals(""))
		{
			if (rubric2.getCode().equals(""))
				return rubric1.getName().compareTo(rubric2.getName());
			else
				return -1;
		}
		else
		{
			if (rubric2.getCode().equals(""))
				return 1;
			else
				return rubric1.getCode().compareTo(rubric2.getCode());
		}
	}
}
