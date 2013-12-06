package ch.eugster.events.domain.editors;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class DomainEditorInput extends AbstractEntityEditorInput<Domain>
{
	public DomainEditorInput(Domain domain)
	{
		entity = domain;
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
