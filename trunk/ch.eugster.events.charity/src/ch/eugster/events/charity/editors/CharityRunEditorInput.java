package ch.eugster.events.charity.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CharityRunEditorInput extends AbstractEntityEditorInput<CharityRun>
{

	public CharityRunEditorInput(CharityRun charityRun)
	{
		this.entity = charityRun;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public AbstractEntity getParent()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return "TTT";
	}

	@Override
	public String getToolTipText()
	{
		return "TTT";
	}

}
