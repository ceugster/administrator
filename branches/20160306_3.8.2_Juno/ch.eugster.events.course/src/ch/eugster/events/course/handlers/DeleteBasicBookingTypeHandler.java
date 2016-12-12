package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import ch.eugster.events.persistence.model.BookingTypeProposition;
import ch.eugster.events.persistence.model.PaymentTerm;
import ch.eugster.events.persistence.queries.BookingTypePropositionQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteBasicBookingTypeHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
				IWorkbenchPart part = (IWorkbenchPart) context.getVariable("activePart");
				if (!ssel.isEmpty() && ssel.size() == 1)
				{
					if (ssel.getFirstElement() instanceof BookingTypeProposition)
					{
						BookingTypeProposition basicBookingType = (BookingTypeProposition) ssel.getFirstElement();
						Shell shell = part.getSite().getShell();
						String title = "Löschbestätigung";
						StringBuilder msg = new StringBuilder("Soll die ausgewählte Buchungsart entfernt werden?");
						int icon = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons, 0);
						if (dialog.open() == 0)
						{
							if (connectionService != null)
							{
								BookingTypePropositionQuery query = (BookingTypePropositionQuery) connectionService
										.getQuery(BookingTypeProposition.class);
								basicBookingType = query.delete(basicBookingType);
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getParent().getVariable("selection") instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) context.getVariable("selection");
			this.setBaseEnabled(ssel.getFirstElement() instanceof PaymentTerm);
		}
	}

}
