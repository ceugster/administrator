package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.queries.BookingQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteBookingHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
		if (ssel != null && ssel.getFirstElement() instanceof Booking)
		{
			Shell shell = (Shell) context.getParent().getVariable("activeShell");
			MessageDialog dialog = new MessageDialog(shell, "Buchung entfernen", null,
					"Soll die ausgewählte Buchung entfernt werden?", MessageDialog.QUESTION, new String[] { "Ja",
							"Nein" }, 0);
			if (dialog.open() == 0)
			{
				Booking booking = (Booking) ssel.getFirstElement();

				if (connectionService != null)
				{
					BookingQuery query = (BookingQuery) connectionService.getQuery(Booking.class);
					booking = query.delete(booking);
				}
			}
		}
		return null;
	}

}
