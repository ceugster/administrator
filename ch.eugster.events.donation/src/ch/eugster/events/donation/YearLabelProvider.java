package ch.eugster.events.donation;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.DonationYear;

public class YearLabelProvider extends LabelProvider
{
	@Override
	public String getText(Object element)
	{
		if (element instanceof DonationYear)
		{
			DonationYear donationYear = (DonationYear) element;
			if (donationYear.isAll())
			{
				return "Alle";
			}
			return Integer.toString(donationYear.getYear());
		}
		return "";
	}

}
