package ch.eugster.events.rubric.editors;

import ch.eugster.events.persistence.model.Rubric;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class RubricEditorInput extends AbstractEntityEditorInput<Rubric>
{
	public RubricEditorInput(Rubric rubric)
	{
		entity = rubric;
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
