package ch.eugster.events.visits.editors;

import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class VisitThemeEditorInput extends AbstractEntityEditorInput<VisitTheme>
{
	public VisitThemeEditorInput(VisitTheme visitTheme)
	{
		entity = visitTheme;
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
