package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.queries.BookingQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteBookingHandler extends AbstractHandler implements IHandler
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

				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ConnectionService.class.getName(), null);
				tracker.open();
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					BookingQuery query = (BookingQuery) service.getQuery(Booking.class);
					booking = query.delete(booking);
				}
				tracker.close();
			}
		}
		return null;
	}

}
