package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.PersonTitle;

public class TitleComboViewerContentProvider extends ArrayContentProvider
{
	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof String[])
		{
			return (String[]) inputElement;
		}
		else if (inputElement instanceof PersonTitle[])
		{
			return (PersonTitle[]) inputElement;
		}
		return new PersonTitle[0];
	}

}
