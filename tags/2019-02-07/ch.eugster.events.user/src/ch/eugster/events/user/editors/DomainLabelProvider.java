package ch.eugster.events.user.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.user.Activator;

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
			return domain.getCode().equals("") ? domain.getName() : domain.getCode() + " - " + domain.getName();
		}
		return "";
	}

}
