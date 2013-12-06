package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ProvinceSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{
		String zc1 = (String) element1;
		String zc2 = (String) element2;

		return zc1.compareTo(zc2);
	}
}
