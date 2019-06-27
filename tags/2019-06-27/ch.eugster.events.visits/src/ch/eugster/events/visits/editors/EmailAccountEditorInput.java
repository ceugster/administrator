package ch.eugster.events.visits.editors;

import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class EmailAccountEditorInput extends AbstractEntityEditorInput<EmailAccount>
{
	public EmailAccountEditorInput(final EmailAccount emailAccount)
	{
		entity = emailAccount;
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

	@Override
	public boolean hasParent()
	{
		return false;
	}
}
