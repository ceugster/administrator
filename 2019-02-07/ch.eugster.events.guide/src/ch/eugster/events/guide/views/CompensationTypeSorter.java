package ch.eugster.events.guide.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.CompensationType;

public class CompensationTypeSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		CompensationType type1 = (CompensationType) e1;
		CompensationType type2 = (CompensationType) e2;

		if (type1.getCode().equals(type2.getCode()))
			return type1.getName().compareTo(type2.getName());
		else
			return type1.getCode().compareTo(type2.getCode());
	}
}
