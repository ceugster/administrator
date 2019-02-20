package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.PersonSex;

public class PersonSexLabelProvider extends LabelProvider
{

	@Override
	public String getText(Object element)
	{
		if (element instanceof PersonSex)
			return ((PersonSex) element).getSalutation();
		return "";
	}

}
