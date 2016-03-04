package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.PersonForm;

public class PersonFormLabelProvider extends LabelProvider
{

	@Override
	public String getText(Object element)
	{
		if (element instanceof PersonForm)
			return ((PersonForm) element).toString();
		return "";
	}

}
