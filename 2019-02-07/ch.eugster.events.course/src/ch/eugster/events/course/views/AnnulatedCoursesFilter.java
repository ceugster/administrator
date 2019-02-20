package ch.eugster.events.course.views;

import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.State;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;

public class AnnulatedCoursesFilter extends ViewerFilter implements IStateListener
{
	private boolean doFilter = false;

	private Viewer viewer;

	public AnnulatedCoursesFilter(Viewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (this.doFilter)
		{
			if (element instanceof Course)
			{
				Course course = (Course) element;
				return !course.getState().equals(CourseState.ANNULATED);
			}
		}

		return true;
	}

	public void doFilter(boolean showAll)
	{
		this.doFilter = !showAll;
	}

	@Override
	public void handleStateChange(State state, Object oldValue)
	{
		this.doFilter(((Boolean) state.getValue()).booleanValue());
		this.viewer.refresh();
	}

}
