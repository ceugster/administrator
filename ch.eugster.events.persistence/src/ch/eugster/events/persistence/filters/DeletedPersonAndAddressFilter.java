package ch.eugster.events.persistence.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Person;

public class DeletedPersonAndAddressFilter extends ViewerFilter
{
	private boolean showDeleted;

	public void setShowDeleted(boolean show)
	{
		this.showDeleted = show;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (showDeleted)
		{
			if (element instanceof Person || element instanceof Address)
			{
				return true;
			}
		}

		return !((AbstractEntity) element).isDeleted();
	}

}
