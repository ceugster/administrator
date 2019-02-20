package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.AddressType;

public class AddressTypeLabelProvider extends LabelProvider
{
	@Override
	public Image getImage(Object element)
	{
		if (element instanceof AddressType)
		{
			AddressType addressType = (AddressType) element;
			return addressType.getImage();
		}
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof AddressType)
		{
			AddressType addressType = (AddressType) element;
			return addressType.getName();
		}
		return "";
	}

}
