package ch.eugster.events.donation.handlers;

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

import ch.eugster.events.donation.Activator;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteDonationHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteDonationHandler()
	{
		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null);
				return connectionService;
			}

			@Override
			public void removedService(ServiceReference reference, Object service)
			{
				connectionService = null;
				setBaseEnabled(false);
				super.removedService(reference, service);
			}
		};
		connectionServiceTracker.open();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty())
			{
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				String title = "Spende entfernen";
				String msg = "Soll die ausgewählte Spende entfernt werden?";
				int type = MessageDialog.QUESTION;
				String[] buttons = new String[] { "Ja", "Nein" };
				MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
				if (dialog.open() == 0)
				{
					Iterator<?> iterator = ssel.iterator();
					while (iterator.hasNext())
					{
						Object object = iterator.next();
						if (object instanceof Donation)
						{
							if (connectionService != null)
							{
								DonationQuery query = (DonationQuery) connectionService.getQuery(Donation.class);
								object = query.delete((Donation) object);
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		Object object = context.getVariable("selection");
		if (object instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) object;
			enabled = ssel.getFirstElement() instanceof Donation;
		}
		else
		{
			System.out.println();
		}
		setBaseEnabled(enabled);
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();
	}
}
