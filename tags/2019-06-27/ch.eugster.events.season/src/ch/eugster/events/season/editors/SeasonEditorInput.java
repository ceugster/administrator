package ch.eugster.events.season.editors;

import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class SeasonEditorInput extends AbstractEntityEditorInput<Season>
{
	public SeasonEditorInput(Season season)
	{
		entity = season;
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
