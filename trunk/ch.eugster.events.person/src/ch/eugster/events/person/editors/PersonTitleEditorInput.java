package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class PersonTitleEditorInput extends
		AbstractEntityEditorInput<PersonTitle> {

	public PersonTitleEditorInput(PersonTitle personTitle)
	{
		this.setEntity(personTitle);
	}
	
	@Override
	public boolean hasParent() {
		return false;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getToolTipText() {
		return "";
	}

}
