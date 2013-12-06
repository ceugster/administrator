package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.Country;

public class CountryPrefixLabelProvider extends LabelProvider
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
			return ((Country) element).getPhonePrefix();
		}
		return "";
	}

}
