package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class TitleComboViewerSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof String && e2 instanceof String)
		{
			String s1 = (String) e1;
			String s2 = (String) e2;

			return s1.compareTo(s2);
		}
		return 0;
	}

}
