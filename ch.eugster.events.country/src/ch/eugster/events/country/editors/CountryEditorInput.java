package ch.eugster.events.country.editors;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CountryEditorInput extends AbstractEntityEditorInput<Country>
{
	public CountryEditorInput(Country country)
	{
		entity = country;
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
