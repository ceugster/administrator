package ch.eugster.events.course.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.ActionFactory;

import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.helpers.ClipboardHelper;

public class CourseCopyAction extends Action implements IAction
{
	private StructuredViewer viewer;

	public CourseCopyAction(StructuredViewer viewer, String label, int accelerator)
	{
		super(label);
		this.viewer = viewer;
		this.setAccelerator(accelerator);
		this.setActionDefinitionId(ActionFactory.COPY.getCommandId());
		this.setId(ActionFactory.COPY.getId());
	}

	@Override
	public void run()
	{
		if (this.viewer != null)
		{
			if (this.viewer.getSelection() instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) this.viewer.getSelection();
				if (ssel.size() > 0)
				{
					CourseTransfer transfer = CourseTransfer.getTransfer();
					Object[] selection = ssel.toArray();
					transfer.setData(DND.DROP_COPY, selection);
					ClipboardHelper.getClipboard().setContents(new Object[] { selection }, new Transfer[] { transfer });
				}
			}
		}
	}

	@Override
	public boolean isEnabled()
	{
		if (this.viewer != null)
		{
			if (this.viewer.getSelection() instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) this.viewer.getSelection();
				return ssel.size() > 0;
			}
		}
		return false;
	}

}
