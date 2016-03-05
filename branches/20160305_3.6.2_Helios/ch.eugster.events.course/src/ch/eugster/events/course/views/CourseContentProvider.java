package ch.eugster.events.course.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.SeasonQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseContentProvider implements ITreeContentProvider
{
	public CourseContentProvider()
	{
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getChildren(final Object parentElement)
	{
		if (parentElement instanceof ConnectionService)
		{
			ConnectionService con = (ConnectionService) parentElement;
			SeasonQuery query = (SeasonQuery) con.getQuery(Season.class);
			return query.selectAll().toArray(new Season[0]);
		}
		else if (parentElement instanceof Season)
		{
			Season season = (Season) parentElement;
			return season.getCourses().toArray(new Course[0]);
		}
		return new Season[0];
	}

	@Override
	public Object[] getElements(final Object inputElement)
	{
		return this.getChildren(inputElement);
	}

	@Override
	public Object getParent(final Object element)
	{
		if (element instanceof Course)
		{
			Course course = (Course) element;
			return course.getSeason();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element)
	{
		if (element instanceof Season)
		{
			Season season = (Season) element;
			return season.getCourses().size() > 0;
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}

}
