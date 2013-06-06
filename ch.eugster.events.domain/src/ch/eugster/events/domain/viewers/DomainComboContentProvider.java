package ch.eugster.events.domain.viewers;

import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Domain;

public class DomainComboContentProvider extends ArrayContentProvider
{
	private boolean mandatoryDomain;

	public DomainComboContentProvider(final boolean mandatory)
	{
		this.mandatoryDomain = mandatory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(final Object inputElement)
	{
		Domain[] domains = new Domain[0];
		if (this.mandatoryDomain)
		{
			if (inputElement instanceof Domain[])
			{
				domains = (Domain[]) inputElement;
			}
			else if (inputElement instanceof Collection<?>)
			{
				domains = ((Collection<Domain>) inputElement).toArray(new Domain[0]);
			}
		}
		else if (inputElement instanceof Domain[])
		{
			Domain[] list = (Domain[]) inputElement;
			domains = new Domain[list.length + 1];
			for (int i = 0; i < list.length; i++)
				domains[i + 1] = list[i];
			domains[0] = Domain.newInstance();
		}
		else if (inputElement instanceof Collection<?>)
		{
			Domain[] list = ((Collection<Domain>) inputElement).toArray(new Domain[0]);
			domains = new Domain[list.length + 1];
			for (int i = 0; i < list.length; i++)
				domains[i + 1] = list[i];
			domains[0] = Domain.newInstance();
		}
		return domains;
	}

}
