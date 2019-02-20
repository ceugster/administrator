package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.PersonTitle;

public class PersonTitleSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof PersonTitle && e2 instanceof PersonTitle)
		{
			PersonTitle title1 = (PersonTitle) e1;
			PersonTitle title2 = (PersonTitle) e2;
			return title1.getTitle().compareTo(title2.getTitle());
		}
		else if (e1 instanceof String && e2 instanceof String)
		{
			String title1 = (String) e1;
			String title2 = (String) e2;
			return title1.compareTo(title2);
		}
		return 0;
	}
}
