package ch.eugster.events.course.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.queries.ParticipantQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteParticipantHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteParticipantHandler()
	{
		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null);
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				connectionService = null;
				setBaseEnabled(false);
				super.removedService(reference, service);
			}

		};
		connectionServiceTracker.open();
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
		if (ssel != null && ssel.getFirstElement() instanceof Participant)
		{
			Participant participant = (Participant) ssel.getFirstElement();
			if (!participant.getId().equals(participant.getBooking().getParticipant().getId()))
			{
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				MessageDialog dialog = new MessageDialog(shell, "Teilnehmer entfernen", null,
						"Soll der gewählte Teilnehmer entfernt werden?", MessageDialog.QUESTION, new String[] { "Ja",
								"Nein" }, 0);
				if (dialog.open() == 0)
				{
					ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
							ConnectionService.class.getName(), null);
					tracker.open();
					ConnectionService service = (ConnectionService) tracker.getService();
					if (service != null)
					{
						ParticipantQuery query = (ParticipantQuery) service.getQuery(Participant.class);
						participant = query.delete(participant);
					}
					tracker.close();
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		Long bookingId = null;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getParent().getVariable("selection") instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) context.getVariable("selection");
			@SuppressWarnings("unchecked")
			Iterator<Object> iterator = ssel.iterator();
			while (iterator.hasNext())
			{
				Object object = iterator.next();
				if (object instanceof Participant)
				{
					Participant participant = (Participant) object;
					if (participant.getId().equals(participant.getBooking().getParticipant().getId()))
					{
						break;
					}
					if (bookingId == null)
					{
						bookingId = participant.getBooking().getId();
						enabled = true;
					}
					else
					{
						if (!participant.getBooking().getId().equals(bookingId))
						{
							enabled = false;
							break;
						}
					}
				}
				else
				{
					enabled = false;
					break;
				}
			}
		}
		this.setBaseEnabled(enabled);
	}

}
