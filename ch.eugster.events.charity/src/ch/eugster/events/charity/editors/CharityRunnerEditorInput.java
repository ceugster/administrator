package ch.eugster.events.charity.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.CharityRunner;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CharityRunnerEditorInput extends AbstractEntityEditorInput<CharityRunner>
{

	public CharityRunnerEditorInput(CharityRunner charityRunner)
	{
		this.entity = charityRunner;
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
