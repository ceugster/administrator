package ch.eugster.events.course.reporting.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.reporting.dialogs.PrintLabelDialog;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Season;

public class PrintLabelHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			ISelection sel = (ISelection) context.getParent().getVariable("selection");
			IStructuredSelection ssel = (IStructuredSelection) sel;
			Shell shell = (Shell) context.getParent().getVariable("activeShell");

			PrintLabelDialog dialog = new PrintLabelDialog(shell, ssel);
			dialog.open();
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		Object selection = context.getVariable("selection");
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.getFirstElement() instanceof Season)
			{
				enabled = true;
			}
			else if (ssel.getFirstElement() instanceof Course)
			{
				enabled = true;
			}
			else if (ssel.getFirstElement() instanceof Booking)
			{
				enabled = true;
			}
		}
		setBaseEnabled(enabled);
	}
}
