package ch.eugster.events.donation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;

public class DomainFilter extends ViewerFilter
{
	private Domain domain;

	public DomainFilter()
	{
	}
	
	public void setSelectedDomain(Domain domain)
	{
		this.domain = domain;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) 
	{
		if (domain == null) 
		{
			return true;
		}
		if (domain.getName().equals("Alle"))
		{
			return true;
		}
		if (element instanceof Donation) 
		{
			Donation donation = (Donation) element;
			if (domain.getName().equals("Ohne Domäne")) 
			{
				return donation.getDomain() == null;
			}
			return donation.getDomain() != null && donation.getDomain().getId().equals(domain.getId());
		}
		return false;
	}

}
