package ch.eugster.events.persistence.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public abstract class AbstractEntityViewerFilter extends ViewerFilter
{
	@Override
	public abstract boolean select(Viewer viewer, Object parentElement, Object element);

	@Override
	public boolean equals(Object object)
	{
		return object.getClass().equals(this.getClass());
	}
}
