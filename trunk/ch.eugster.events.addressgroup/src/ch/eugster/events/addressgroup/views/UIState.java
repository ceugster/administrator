package ch.eugster.events.addressgroup.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.State;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

public class UIState extends State
{
	@Override
	public void setValue(Object value)
	{
		super.setValue(value);

		Map<String, Map> commandIds = new HashMap<String, Map>();
		for (Object listener : getListeners())
		{
			if (listener instanceof UpdateableRightCommandHandler)
			{
				ICommandInfo handler = (ICommandInfo) listener;
				commandIds.put(handler.getCommandId(), handler.getRefreshFilter());
			}
		}
		if (!commandIds.isEmpty())
		{
			ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			for (Map.Entry<String, Map> entry : commandIds.entrySet())
			{
				service.refreshElements(entry.getKey(), entry.getValue());
			}
		}
	}
}
