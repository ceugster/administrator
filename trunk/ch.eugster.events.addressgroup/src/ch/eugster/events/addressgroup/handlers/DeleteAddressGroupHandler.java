package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteAddressGroupHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteAddressGroupHandler()
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
				super.removedService(reference, service);
				setBaseEnabled(false);
			}
		};
		connectionServiceTracker.open();
	}

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
					if (ssel.getFirstElement() instanceof AddressGroup)
					{
						AddressGroup addressGroup = (AddressGroup) ssel.getFirstElement();
						Shell shell = part.getSite().getShell();
						String title = "L�schbest�tigung";
						StringBuilder msg = new StringBuilder("Soll die ausgew�hlte Adressgruppe ");
						msg = msg.append(addressGroup.getCode().equals("") ? addressGroup.getName() : addressGroup
								.getCode() + " - " + addressGroup.getName());
						msg = msg.append(" entfernt werden?");
						int icon = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons, 0);
						if (dialog.open() == 0)
						{
							if (connectionService != null)
							{
								AddressGroupQuery query = (AddressGroupQuery) connectionService
										.getQuery(AddressGroup.class);
								query.delete(addressGroup);
							}
						}
					}
				}
			}
		}
		return null;
	}

}