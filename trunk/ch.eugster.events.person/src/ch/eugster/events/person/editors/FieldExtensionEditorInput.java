package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class FieldExtensionEditorInput extends AbstractEntityEditorInput<FieldExtension>
{

	public FieldExtensionEditorInput(final FieldExtension extension)
	{
		this.setEntity(extension);
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

	@Override
	public boolean hasParent()
	{
		return false;
	}

}
