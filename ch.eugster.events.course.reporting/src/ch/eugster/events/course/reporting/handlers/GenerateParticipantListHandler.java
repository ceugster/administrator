package ch.eugster.events.course.reporting.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.reporting.dialogs.ParticipantListDialog;
import ch.eugster.events.persistence.model.Course;

public class GenerateParticipantListHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				if (!ssel.isEmpty())
				{
					Shell shell = (Shell) context.getParent().getVariable("activeShell");
					ParticipantListDialog dialog = new ParticipantListDialog(shell, ssel);
					dialog.open();
				}
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object object)
	{
		boolean enabled = false;
		if (object instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) object;
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				IStructuredSelection ssel = (IStructuredSelection) context.getParent().getVariable("selection");
				enabled = ssel != null && ssel.getFirstElement() instanceof Course;
			}
		}
		setBaseEnabled(enabled);
	}
}
