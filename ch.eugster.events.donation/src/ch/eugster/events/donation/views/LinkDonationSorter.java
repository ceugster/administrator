package ch.eugster.events.donation.views;

import java.util.Date;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Donation;

public class LinkDonationSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		Donation d1 = (Donation) e1;
		Donation d2 = (Donation) e2;

		Date date1 = d1.getDonationDate().getTime();
		Date date2 = d2.getDonationDate().getTime();

		return date2.compareTo(date1);
	}

}
