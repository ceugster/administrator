package ch.eugster.events.visits.editors;

import ch.eugster.events.persistence.model.SchoolClass;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class SchoolClassEditorInput extends AbstractEntityEditorInput<SchoolClass>
{
	public SchoolClassEditorInput(SchoolClass schoolClass)
	{
		entity = schoolClass;
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
