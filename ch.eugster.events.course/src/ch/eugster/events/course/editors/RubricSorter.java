package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Rubric;

public class RubricSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof Rubric)
		{
			if (e2 instanceof Rubric)
			{
				Rubric rubric1 = (Rubric) e1;
				Rubric rubric2 = (Rubric) e2;

				if (rubric1.getCode().equals(rubric2.getCode()))
					return rubric1.getName().compareTo(rubric2.getName());
				else
					return rubric1.getCode().compareTo(rubric2.getCode());
			}
		}
		return 0;
	}

}
