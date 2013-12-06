package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ProvinceLabelProvider extends LabelProvider
{
	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		return element.toString();
	}

}
