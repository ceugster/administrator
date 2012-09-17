package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Domain;

public class DomainComboSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof Domain)
		{
			if (e2 instanceof Domain)
			{
				Domain domain1 = (Domain) e1;
				Domain domain2 = (Domain) e2;

				if (domain1.getCode().equals(domain2.getCode()))
					return domain1.getName().compareTo(domain2.getName());
				else
					return domain1.getCode().compareTo(domain2.getCode());
			}
		}
		return 0;
	}

}
