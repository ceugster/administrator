package ch.eugster.events.course.views;

import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.State;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Season;

public class SeasonFilter extends ViewerFilter implements IStateListener
{
	private boolean doFilter = false;

	private final Viewer viewer;

	public SeasonFilter(Viewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (this.doFilter)
		{
			if (element instanceof Season)
			{
				Season season = (Season) element;
				return !season.isClosed();
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
		if (this.viewer != null && !this.viewer.getControl().isDisposed())
		{
			this.doFilter(((Boolean) state.getValue()).booleanValue());
			this.viewer.refresh();
		}
	}

}
