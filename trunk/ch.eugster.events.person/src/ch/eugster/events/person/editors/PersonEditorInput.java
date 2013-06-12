package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class PersonEditorInput extends AbstractEntityEditorInput<Person>
{
	public PersonEditorInput(final Person person)
	{
		this.entity = person;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class clazz)
	{
		if (clazz.equals(Person.class))
		{
			return this.entity;
		}
		return super.getAdapter(clazz);
	}

	@Override
	public String getName()
	{
		if (entity.getId() == null)
		{
			return "Neu";
		}
		else
		{
			return PersonFormatter.getInstance().formatLastnameFirstname(entity);
		}
	}

	@Override
	public String getToolTipText()
	{
		return getName();
	}

	@Override
	public boolean hasParent()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
