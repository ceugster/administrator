package ch.eugster.events.addressgroup.handlers;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

import ch.eugster.events.addressgroup.dnd.AddressGroupMemberTransfer;
import ch.eugster.events.addressgroup.dnd.AddressGroupTransfer;
import ch.eugster.events.addressgroup.views.AddressGroupMemberView;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.ui.helpers.ClipboardHelper;

public class CopyHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			Object activePart = context.getVariable("activePart");
			if (activePart instanceof AddressGroupView)
			{
				AddressGroupView view = (AddressGroupView) activePart;
				TreeViewer viewer = view.getViewer();
				StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
				Object[] elements = ssel.toArray();

				Collection<AddressGroup> groups = new ArrayList<AddressGroup>();
				for (Object element : elements)
				{
					if (element instanceof AddressGroup)
					{
						groups.add((AddressGroup) element);
					}
				}
				setContent(groups.toArray(new AddressGroup[0]));
			}
			else if (activePart instanceof AddressGroupMemberView)
			{
				AddressGroupMemberView view = (AddressGroupMemberView) activePart;
				TableViewer viewer = view.getViewer();
				StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
				Object[] elements = ssel.toArray();

				Collection<AddressGroupMember> members = new ArrayList<AddressGroupMember>();
				for (Object element : elements)
				{
					if (element instanceof AddressGroupMember)
					{
						members.add((AddressGroupMember) element);
					}
				}
				setContent(members.toArray(new AddressGroupMember[0]));
			}
		}
		return Status.OK_STATUS;
	}

	private boolean isValidSelection(final AddressGroupMemberView view)
	{
		TableViewer viewer = view.getViewer();
		if (viewer.getSelection() instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
			return !ssel.isEmpty();
		}
		return false;
	}

	private boolean isValidSelection(final AddressGroupView view)
	{
		TreeViewer viewer = view.getViewer();
		if (viewer.getSelection() instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
			Object[] elements = ssel.toArray();

			for (Object element : elements)
			{
				if (!(element instanceof AddressGroup))
				{
					return false;
				}
			}
		}
		return true;
	}

	private void setContent(final AddressGroup[] groups)
	{
		Transfer[] transfers = new Transfer[groups.length];
		for (int i = 0; i < groups.length; i++)
		{
			AddressGroupTransfer transfer = AddressGroupTransfer.getTransfer();
			transfer.setData(DND.DROP_COPY, groups);
			transfers[i] = transfer;
		}
		ClipboardHelper.getClipboard().setContents(groups, transfers);
	}

	private void setContent(final AddressGroupMember[] members)
	{
		Transfer[] transfers = new Transfer[members.length];
		for (int i = 0; i < members.length; i++)
		{
			AddressGroupMemberTransfer transfer = AddressGroupMemberTransfer.getTransfer();
			transfer.setData(DND.DROP_COPY, members);
			transfers[i] = transfer;
		}
		ClipboardHelper.getClipboard().setContents(members, transfers);
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		if (evaluationContext instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) evaluationContext;
			Object activePart = context.getVariable("activePart");
			if (activePart instanceof AddressGroupView)
			{
				enabled = isValidSelection((AddressGroupView) activePart);
			}
			else if (activePart instanceof AddressGroupMemberView)
			{
				enabled = isValidSelection((AddressGroupMemberView) activePart);
			}
		}
		setBaseEnabled(enabled);
	}

}
