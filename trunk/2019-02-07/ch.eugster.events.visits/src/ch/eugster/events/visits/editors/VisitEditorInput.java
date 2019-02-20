package ch.eugster.events.visits.editors;

import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class VisitEditorInput extends AbstractEntityEditorInput<Visit>
{
	public VisitEditorInput(Visit visit)
	{
		entity = visit;
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
