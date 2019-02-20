package ch.eugster.events.season.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Season;

public class SeasonSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		Season season1 = (Season) e1;
		Season season2 = (Season) e2;

		if (season1.getStart() == null)
		{
			if (season2.getStart() == null)
				return this.compareActive(season1, season2);
			else
				return this.compareActive(season1, season2);
		}
		else
		{
			if (season2.getStart() == null)
			{
				return -1;
			}
			else
			{
				return season1.getStart().compareTo(season2.getStart());
			}
		}
	}

	private int compareActive(Season season1, Season season2)
	{
		if (season1.isClosed() && season2.isClosed())
			return 0;
		else if (season1.isClosed())
			return 1;
		else if (season2.isClosed())
			return -1;
		else
			return 0;
	}

}
