package ch.eugster.events.donation.views;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.DonationPurpose;

public class DonationPurposeLabelProvider extends LabelProvider
{
	@Override
	public String getText(Object element)
	{
		if (element instanceof DonationPurpose)
		{
			DonationPurpose purpose = (DonationPurpose) element;
			return !purpose.getCode().isEmpty() ? purpose.getCode() + " - " + purpose.getName() : purpose.getName();
		}
		return "";
	}
}
