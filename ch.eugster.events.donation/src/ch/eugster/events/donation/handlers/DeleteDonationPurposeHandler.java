package ch.eugster.events.donation.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.donation.Activator;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.queries.DonationPurposeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteDonationPurposeHandler extends AbstractHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteDonationPurposeHandler()
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
				super.removedService(reference, service);
				setBaseEnabled(false);
			}
		};
		connectionServiceTracker.open();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
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
					if (ssel.getFirstElement() instanceof DonationPurpose)
					{
						DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
						Shell shell = part.getSite().getShell();
						String title = "Löschbestätigung";
						StringBuilder msg = new StringBuilder("Soll die ausgewählte Domäne ");
						msg = msg.append(purpose.getCode().equals("") ? purpose.getName() : purpose.getCode() + " - "
								+ purpose.getName());
						msg = msg.append(" entfernt werden?");
						int icon = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons, 0);
						if (dialog.open() == 0)
						{
							if (connectionService != null)
							{
								DonationPurposeQuery query = (DonationPurposeQuery) connectionService
										.getQuery(DonationPurpose.class);
								query.delete(purpose);
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();
	}
}
