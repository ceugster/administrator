package ch.eugster.events.visits.editors;

import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class ApplianceEditorInput extends AbstractEntityEditorInput<Appliance>
{
	public ApplianceEditorInput(Appliance appliance)
	{
		entity = appliance;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return Appliance.stringValueOf(entity.getName()).isEmpty() ? "Neu" : entity.getName();
	}

	@Override
	public String getToolTipText()
	{
		return Appliance.stringValueOf(entity.getName()).isEmpty() ? "Neu" : entity.getName();
	}
}
