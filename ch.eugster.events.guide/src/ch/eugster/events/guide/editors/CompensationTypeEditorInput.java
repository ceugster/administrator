package ch.eugster.events.guide.editors;

import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CompensationTypeEditorInput extends AbstractEntityEditorInput<CompensationType>
{
	public CompensationTypeEditorInput(CompensationType compensationType)
	{
		this.entity = compensationType;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return "TTT";
	}

	@Override
	public String getToolTipText()
	{

		return "FFF";
	}
}
