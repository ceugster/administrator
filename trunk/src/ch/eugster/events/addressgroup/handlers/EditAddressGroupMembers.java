package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.addressgroup.dialogs.AddressGroupMemberDialog;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class EditAddressGroupMembers extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) context.getParent().getVariable("activeWorkbenchWindowShell");
		StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("activeMenuSelection");
		if (!ssel.isEmpty())
		{
			LinkPersonAddress link = null;
			Address address = null;
			if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				link = (LinkPersonAddress) ssel.getFirstElement();
			}
			else if (ssel.getFirstElement() instanceof Person)
			{
				Person person = (Person) ssel.getFirstElement();
				link = person.getDefaultLink();
			}
			else if (ssel.getFirstElement() instanceof Address)
			{
				address = ((Address) ssel.getFirstElement());
			}
			else if (ssel.getFirstElement() instanceof AddressGroupMember)
			{
				AddressGroupMember member = ((AddressGroupMember) ssel.getFirstElement());
				link = member.getLink();
				if (link == null)
				{
					address = member.getAddress();
				}

			}
			AddressGroupMemberDialog dialog = null;
			if (link != null)
			{
				dialog = new AddressGroupMemberDialog(shell, link);
			}
			else if (address != null)
			{
				dialog = new AddressGroupMemberDialog(shell, address);
			}
			if (dialog != null)
			{
				dialog.open();
			}
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
			if (ssel != null && !ssel.isEmpty())
			{
				Object object = ssel.getFirstElement();
				if (object instanceof LinkPersonAddress || object instanceof Address
						|| object instanceof AddressGroupMember || object instanceof Person)
				{
					enabled = true;
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
