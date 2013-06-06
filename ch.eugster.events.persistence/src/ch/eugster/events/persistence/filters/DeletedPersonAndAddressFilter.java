package ch.eugster.events.persistence.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.AbstractEntity;

public class DeletedPersonAndAddressFilter extends ViewerFilter
{
	private boolean showDeleted;

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element)
	{
		if (showDeleted)
		{
			return true;
		}
		return !((AbstractEntity) element).isDeleted();
	}

	public void setShowDeleted(final boolean show)
	{
		this.showDeleted = show;
	}

}
