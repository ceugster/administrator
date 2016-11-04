package ch.eugster.events.visits.editors;

import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class SchoolLevelEditorInput extends AbstractEntityEditorInput<SchoolLevel>
{
	public SchoolLevelEditorInput(SchoolLevel schoolLevel)
	{
		entity = schoolLevel;
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
