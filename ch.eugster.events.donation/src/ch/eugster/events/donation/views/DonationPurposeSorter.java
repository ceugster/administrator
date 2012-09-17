package ch.eugster.events.donation.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.DonationPurpose;

public class DonationPurposeSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof DonationPurpose && e2 instanceof DonationPurpose)
		{
			DonationPurpose dp1 = (DonationPurpose) e1;
			DonationPurpose dp2 = (DonationPurpose) e2;

			if (dp1.getCode().equals(""))
			{
				if (dp2.getCode().equals(""))
					return dp1.getName().compareTo(dp2.getName());
				else
					return -1;
			}
			else
			{
				if (dp2.getCode().equals(""))
					return 1;
				else
					return dp1.getCode().compareTo(dp2.getCode());
			}
		}
		return 0;
	}
}
