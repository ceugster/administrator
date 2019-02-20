package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.dialogs.SelectBookingTypeDialog;
import ch.eugster.events.persistence.model.Participant;

public class SetBookingTypeHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
		if (ssel != null && ssel.size() == 1 && ssel.getFirstElement() instanceof Participant)
		{
			Shell shell = (Shell) context.getParent().getVariable("activeShell");
			SelectBookingTypeDialog dialog = new SelectBookingTypeDialog(shell, (Participant) ssel.getFirstElement());
			dialog.open();
		}
		return null;
	}

}
