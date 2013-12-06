package ch.eugster.events.country.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.Country;

public class CountryLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof Country)
		{
			Country country = (Country) element;
			return country.getIso3166alpha2() + " - " + country.getName();
		}
		return "";
	}

}
