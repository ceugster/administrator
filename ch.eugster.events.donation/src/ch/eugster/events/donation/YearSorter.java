package ch.eugster.events.donation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.DonationYear;

public class YearSorter extends ViewerSorter 
{
	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) 
	{
		if (e1 instanceof DonationYear && e2 instanceof DonationYear) {
			DonationYear d1 = (DonationYear) e1;
			DonationYear d2 = (DonationYear) e2;

			return Integer.valueOf(d2.getYear()).compareTo(
					Integer.valueOf(d1.getYear()));
		}
		return 0;
	}
}
