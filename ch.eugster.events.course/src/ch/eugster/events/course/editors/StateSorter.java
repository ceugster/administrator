package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.CourseState;

public class StateSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		CourseState state1 = (CourseState) e1;
		CourseState state2 = (CourseState) e2;

		return new Integer(state1.ordinal()).compareTo(new Integer(state2.ordinal()));
	}

}
