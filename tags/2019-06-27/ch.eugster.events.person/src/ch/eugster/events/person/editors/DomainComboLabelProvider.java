package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.Domain;

public class DomainComboLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		StringBuilder builder = new StringBuilder("");
		if (element instanceof Domain)
		{
			Domain domain = (Domain) element;
			if (!domain.getCode().isEmpty())
			{
				builder = builder.append(domain.getCode());
				if (!domain.getName().isEmpty())
					builder = builder.append(" - ");
			}
			if (!domain.getName().isEmpty())
				builder = builder.append(domain.getName());
		}
		return builder.toString();
	}

}
