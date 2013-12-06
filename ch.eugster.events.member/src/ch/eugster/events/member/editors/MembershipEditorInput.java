package ch.eugster.events.member.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class MembershipEditorInput extends AbstractEntityEditorInput<Membership>
{
	public MembershipEditorInput(Membership entity)
	{
		this.entity = entity;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public AbstractEntity getParent()
	{
		return null;
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
