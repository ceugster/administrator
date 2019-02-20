package ch.eugster.events.visits.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.VisitTheme;

public class VisitThemeSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		VisitTheme visitTheme1 = (VisitTheme) e1;
		VisitTheme visitTheme2 = (VisitTheme) e2;

		return visitTheme1.getName().compareTo(visitTheme2.getName());
	}
}
