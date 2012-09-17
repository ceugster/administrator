package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.Activator;

public class PersonLabelProvider extends LabelProvider
{
	@Override
	public Image getImage(final Object element)
	{
		if (element instanceof Person)
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE);
		}
		else if (element instanceof Address)
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS);
		}
		return null;
	}

	@Override
	public String getText(final Object element)
	{
		if (element instanceof Person)
		{
			Person person = (Person) element;
			return PersonFormatter.getInstance().formatLastnameFirstname(person);
		}
		else if (element instanceof Address)
		{
			Address address = (Address) element;
			return address.getAddress();
		}
		else
			return "";
	}

}
