package ch.eugster.events.addressgroup.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.eugster.events.addressgroup.AddressGroupMemberParentType;
import ch.eugster.events.addressgroup.views.PersonAddressGroupMemberView;

public class SetAddressGroupSelectionLinkHandler extends AbstractHandler implements IHandler, IElementUpdater
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			IWorkbenchPart part = (IWorkbenchPart) context.getVariable("activePart");
			if (part instanceof PersonAddressGroupMemberView)
			{
				PersonAddressGroupMemberView view = (PersonAddressGroupMemberView) part;
				view.setMode(AddressGroupMemberParentType.LINK_PERSON_ADDRESS);
				ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = service.getCommand("ch.eugster.events.addressgroup.command.set.addressgroup.selection.link");
				State state = command.getState("ch.eugster.events.addressgroup.person.view.selection.link.state");
				state.setValue(view.getMode().equals(AddressGroupMemberParentType.LINK_PERSON_ADDRESS));
				service.refreshElements(event.getCommand().getId(), null);
				command = service.getCommand("ch.eugster.events.addressgroup.command.set.addressgroup.selection.address");
				state = command.getState("ch.eugster.events.addressgroup.person.view.selection.address.state");
				state.setValue(view.getMode().equals(AddressGroupMemberParentType.ADDRESS));
				service.refreshElements("ch.eugster.events.addressgroup.command.set.addressgroup.selection.address", null);
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map map)
	{
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand("ch.eugster.events.addressgroup.command.set.addressgroup.selection.link");
		State state = command.getState("ch.eugster.events.addressgroup.person.view.selection.link.state");
		element.setChecked(((Boolean) state.getValue()).booleanValue());
	}

}
