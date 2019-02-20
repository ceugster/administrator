package ch.eugster.events.zipcode.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.ZipCode;

public class ZipCodeLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof ZipCode)
		{
			ZipCode zipCode = (ZipCode) element;
			return zipCode.getZip() + " - " + zipCode.getCity();
		}
		return "";
	}

}
