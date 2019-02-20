package ch.eugster.events.zipcode.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.ZipCode;

public class ZipCodeSorter extends ViewerSorter
{

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof ZipCode && e2 instanceof ZipCode)
		{
			ZipCode zipCode1 = (ZipCode) e1;
			ZipCode zipCode2 = (ZipCode) e2;

			return zipCode1.getZip().compareTo(zipCode2.getZip());
		}
		return 0;
	}
}
