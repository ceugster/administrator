package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import ch.eugster.events.addressgroup.views.AddressGroupMemberView;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class SelectAllHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			Object activePart = context.getVariable("activePart");
			if (activePart instanceof AddressGroupMemberView)
			{
				AddressGroupMemberView view = (AddressGroupMemberView) activePart;
				TableViewer viewer = view.getViewer();
				if (viewer.getInput() instanceof AddressGroup)
				{
					AddressGroup addressGroup = (AddressGroup) viewer.getInput();
					AddressGroupMember[] members = addressGroup.getAddressGroupMembers().toArray(
							new AddressGroupMember[0]);
					StructuredSelection ssel = new StructuredSelection(members);
					viewer.setSelection(ssel);

				}
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		setBaseEnabled(true);
	}

}
