package ch.eugster.events.donation;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.DonationPurpose;

public class PurposeLabelProvider extends LabelProvider
{
	@Override
	public String getText(Object element)
	{
		if (element instanceof DonationPurpose)
		{
			DonationPurpose purpose = (DonationPurpose) element;
			return purpose.getName();
		}
		return "";
	}
}
