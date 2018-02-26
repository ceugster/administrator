package ch.eugster.events.donation;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.DonationYear;

public class YearLabelProvider extends LabelProvider
{
	@Override
	public String getText(final Object element)
	{
		if (element instanceof DonationYear)
		{
			final DonationYear donationYear = (DonationYear) element;
			if (donationYear.getYear() == 0)
			{
				return "<Kein Ausschluss>";
			}
			return Integer.toString(donationYear.getYear());
		}
		return "";
	}
}
