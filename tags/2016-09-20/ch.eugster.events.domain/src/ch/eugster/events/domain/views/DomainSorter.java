package ch.eugster.events.domain.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Domain;

public class DomainSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof Domain && e2 instanceof Domain)
		{
			Domain domain1 = (Domain) e1;
			Domain domain2 = (Domain) e2;

			if (domain1.getCode().equals(""))
			{
				if (domain2.getCode().equals(""))
					return domain1.getName().compareTo(domain2.getName());
				else
					return -1;
			}
			else
			{
				if (domain2.getCode().equals(""))
					return 1;
				else
					return domain1.getCode().compareTo(domain2.getCode());
			}
		}
		return 0;
	}
}
