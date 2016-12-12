package ch.eugster.events.course.reporting.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.reporting.dialogs.CourseListDialog;

public class GenerateCourseListSpreadsheetHandler extends AbstractHandler implements IHandler
{
	protected Shell shell;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			ISelection selection = (ISelection) context.getParent().getVariable("selection");
			shell = (Shell) context.getParent().getVariable("activeShell");
			if (selection instanceof IStructuredSelection)
			{
				IStructuredSelection ssel = (IStructuredSelection) selection;
				if (ssel.isEmpty())
				{
					MessageDialog.openConfirm(shell, "Ungültige Auswahl", "Die vorgenommene Auswahl ist ungültig.");
				}
				else
				{
					CourseListDialog dialog = new CourseListDialog(shell, ssel);
					dialog.open();
				}
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			ISelection selection = (ISelection) context.getVariable("selection");
			enabled = !selection.isEmpty();
		}
		setBaseEnabled(enabled);
	}
}
