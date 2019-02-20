package ch.eugster.events.donation;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.Domain;

public class DomainLabelProvider extends LabelProvider
{
	@Override
	public String getText(Object element)
	{
		if (element instanceof Domain)
		{
			Domain domain = (Domain) element;
			return domain.getName();
		}
		return "";
	}
}
