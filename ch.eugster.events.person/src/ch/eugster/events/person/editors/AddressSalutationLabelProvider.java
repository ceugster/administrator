package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.AddressSalutation;

public class AddressSalutationLabelProvider extends LabelProvider
{
	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof AddressSalutation)
		{
			AddressSalutation addressSalutation = (AddressSalutation) element;
			return addressSalutation.getSalutation();
		}
		return "";
	}

}
