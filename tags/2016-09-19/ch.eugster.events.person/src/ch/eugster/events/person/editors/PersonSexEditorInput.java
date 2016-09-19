package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class PersonSexEditorInput extends AbstractEntityEditorInput<PersonSex>
{

	public PersonSexEditorInput(PersonSex personSex)
	{
		this.setEntity(personSex);
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return "";
	}

	@Override
	public String getToolTipText()
	{
		return "";
	}

}
