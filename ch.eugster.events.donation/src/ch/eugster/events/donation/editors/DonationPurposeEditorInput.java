package ch.eugster.events.donation.editors;

import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class DonationPurposeEditorInput extends AbstractEntityEditorInput<DonationPurpose>
{
	public DonationPurposeEditorInput(DonationPurpose purpose)
	{
		this.entity = purpose;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return "Spendenweck";
	}

	@Override
	public String getToolTipText()
	{

		return "Spendenzweck";
	}
}
