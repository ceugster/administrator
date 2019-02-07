package ch.eugster.events.donation.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class DonationEditorInput extends AbstractEntityEditorInput<Donation>
{
	public DonationEditorInput(Donation entity)
	{
		this.entity = entity;
	}

	@Override
	public boolean hasParent()
	{
		return true;
	}

	@Override
	public AbstractEntity getParent()
	{
		return this.entity.getLink() == null ? this.entity.getAddress() : entity.getLink();
	}

	@Override
	public String getName()
	{
		return "NNN";
	}

	@Override
	public String getToolTipText()
	{
		return "TTT";
	}

}
