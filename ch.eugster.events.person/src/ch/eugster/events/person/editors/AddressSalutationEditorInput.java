package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressSalutationEditorInput extends AbstractEntityEditorInput<AddressSalutation>
{

	public AddressSalutationEditorInput(AddressSalutation salutation)
	{
		this.setEntity(salutation);
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
