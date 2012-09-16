package ch.eugster.events.course.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Season;

public class CourseSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof Season && e2 instanceof Season)
		{
			Season season1 = (Season) e1;
			Season season2 = (Season) e2;
			return this.compareActive(season1, season2);
		}
		else if (e1 instanceof Course && e2 instanceof Course)
		{
			Course course1 = (Course) e1;
			Course course2 = (Course) e2;

			if (course2.getCode().equals(course1.getCode()))
				return course2.getTitle().compareTo(course1.getTitle());
			else
				return course2.getCode().compareTo(course1.getCode());
		}
		return 0;
	}

	private int compareActive(Season season1, Season season2)
	{
		if (season1.isClosed() && season2.isClosed())
		{
			return this.compareStart(season1, season2);
		}
		else if (season1.isClosed())
			return 1;
		else if (season2.isClosed())
			return -1;
		else
			return this.compareStart(season1, season2);
	}

	private int compareStart(Season season1, Season season2)
	{
		if (season1.getStart() == null && season2.getStart() == null)
			return 0;
		else if (season1.getStart() == null)
			return 1;
		else if (season2.getStart() == null)
			return -1;
		else
			return season2.getStart().compareTo(season1.getStart());
	}
}
