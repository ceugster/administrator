package ch.eugster.events.donation.editors;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.Domain;

public class DonationDomainLabelProvider extends LabelProvider
{
	@Override
	public String getText(Object element)
	{
		if (element instanceof Domain)
		{
			Domain domain = (Domain) element;
			return !domain.getCode().isEmpty() ? domain.getCode() + " - " + domain.getName() : domain.getName();
		}
		return "";
	}
}
