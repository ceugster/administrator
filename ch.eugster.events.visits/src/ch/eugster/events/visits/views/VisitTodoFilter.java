package ch.eugster.events.visits.views;

import java.util.Collection;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitVisitor;

public class VisitTodoFilter extends ViewerFilter
{

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (element instanceof Visit)
		{
			Visit visit = (Visit) element;
			if (visit.getStart() == null)
			{
				return true;
			}
			if (visit.getEnd() == null)
			{
				return true;
			}
			if (visit.getTheme() == null)
			{
				return true;
			}
			if (visit.getTeacher() == null)
			{
				return true;
			}
			if (visit.getSchoolClass() == null)
			{
				return true;
			}
			if (visit.getVisitors().isEmpty())
			{
				return true;
			}
			Collection<VisitVisitor> visitors = visit.getVisitors();
			boolean unDeleted = false;
			for (VisitVisitor visitor : visitors)
			{
				if (!visitor.isDeleted())
				{
					unDeleted = true;
				}
			}
			return !unDeleted;
		}
		return true;
	}

}
