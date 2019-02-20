package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.FieldExtension;

public class FieldExtensionSorter extends ViewerSorter
{
	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		if (e1 instanceof FieldExtension && e2 instanceof FieldExtension)
		{
			FieldExtension ext1 = (FieldExtension) e1;
			FieldExtension ext2 = (FieldExtension) e2;
			return ext1.getTarget().compareTo(ext2.getTarget());
		}
		return 0;
	}
}
