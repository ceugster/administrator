package ch.eugster.events.addressgroup.views;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;

public abstract class UpdateableRightCommandHandler extends AbstractHandlerWithState implements IElementUpdater,
		ICommandInfo
{

	public UpdateableRightCommandHandler()
	{
		this.setBaseEnabled(true);
		updateStates();
	}

	@Override
	public Map getRefreshFilter()
	{
		return null;
	}

	@Override
	public Collection<String> getAvailableStateIds()
	{
		return Collections.emptyList();
	}

	private void updateStates()
	{
		Collection<String> availableStateIds = getAvailableStateIds();
		if (availableStateIds != null && !availableStateIds.isEmpty())
		{
			final String commandId = getCommandId();
			if (commandId != null)
			{
				ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = service.getCommand(commandId);
				for (String id : availableStateIds)
				{
					final State state = command.getState(id);
					addState(state.getId(), state);
				}

			}
		}
	}
}
