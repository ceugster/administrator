package ch.eugster.events.donation.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.DonationPurpose;

public class DonationPurposeSorter extends ViewerSorter
{

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		if (e1 instanceof DonationPurpose && e2 instanceof DonationPurpose)
		{
			final DonationPurpose dp1 = (DonationPurpose) e1;
			final DonationPurpose dp2 = (DonationPurpose) e2;
			return Double.valueOf(dp1.getOrder()).compareTo(Double.valueOf(dp2.getOrder()));
		}
		return 0;
	}
}
