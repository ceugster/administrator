package ch.eugster.events.donation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;

public class PurposeFilter extends ViewerFilter
{
	private DonationPurpose purpose;

	public void setSelectedPurpose(DonationPurpose purpose)
	{
		this.purpose = purpose;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) 
	{
		if (this.purpose == null) 
		{
			return true;
		}
		if (this.purpose.getId() == null)
		{
			return true;
		}
		if (element instanceof Donation) 
		{
			Donation donation = (Donation) element;
			return donation.getPurpose().getId().equals(purpose.getId());
		}
		return false;
	}
}
