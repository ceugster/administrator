package ch.eugster.events.user.editors;

import ch.eugster.events.persistence.model.User;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class UserEditorInput extends AbstractEntityEditorInput<User>
{
	public UserEditorInput(User user)
	{
		entity = user;
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
