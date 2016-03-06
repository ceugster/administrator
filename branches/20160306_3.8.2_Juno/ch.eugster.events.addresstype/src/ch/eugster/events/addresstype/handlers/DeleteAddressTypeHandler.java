package ch.eugster.events.addresstype.handlers;

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

import ch.eugster.events.addresstype.Activator;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteAddressTypeHandler extends AbstractHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteAddressTypeHandler()
	{
		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null && User.isCurrentUserAdministrator());
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

	// @Override
	// public void setEnabled(Object evaluationContext)
	// {
	// boolean enabled = false;
	// if (User.getCurrent() instanceof User)
	// {
	// enabled =
	// User.getCurrent().getState().equals(User.UserStatus.ADMINISTRATOR);
	// if (enabled)
	// {
	// EvaluationContext ctx = (EvaluationContext) evaluationContext;
	// Object object = ctx.getParent().getVariable("selection");
	// if (object instanceof StructuredSelection)
	// {
	// StructuredSelection ssel = (StructuredSelection) object;
	// enabled = ssel.getFirstElement() instanceof AddressType;
	// }
	// }
	// }
	// setBaseEnabled(enabled);
	// }

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				IWorkbenchPart part = (IWorkbenchPart) context.getVariable("activePart");
				if (!ssel.isEmpty() && ssel.size() == 1)
				{
					if (ssel.getFirstElement() instanceof AddressType)
					{
						AddressType addressType = (AddressType) ssel.getFirstElement();
						Shell shell = part.getSite().getShell();
						String title = "Löschbestätigung";
						StringBuilder msg = new StringBuilder("Soll die ausgewählte Adressart ");
						msg = msg.append(addressType.getName().equals("") ? "???" : addressType.getName());
						msg = msg.append(" entfernt werden?");
						int icon = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons, 0);
						if (dialog.open() == 0)
						{
							if (connectionService != null)
							{
								AddressTypeQuery query = (AddressTypeQuery) connectionService
										.getQuery(AddressType.class);
								addressType = query.delete(addressType);
							}
						}
					}
				}
			}
		}
		return null;
	}
}
