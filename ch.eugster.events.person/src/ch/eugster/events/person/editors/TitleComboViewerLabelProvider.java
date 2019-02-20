package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.model.PersonTitle;

public class TitleComboViewerLabelProvider extends LabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof String)
		{
			return (String) element;
		}
		else if (element instanceof PersonTitle)
		{
			return ((PersonTitle) element).getTitle();
		}
		return "";
	}

}
