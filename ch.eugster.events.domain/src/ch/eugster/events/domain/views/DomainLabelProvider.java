package ch.eugster.events.domain.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.domain.Activator;
import ch.eugster.events.persistence.model.Domain;

public class DomainLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof Domain)
		{
			return Activator.getDefault().getImageRegistry().get("DOMAIN");
		}
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof Domain)
		{
			Domain domain = (Domain) element;
			return domain.getComboFormat();
		}
		return "";
	}

}
