package ch.eugster.events.member.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.member.Activator;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.queries.MembershipQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteMembershipHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteMembershipHandler()
	{
		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null)
		{
			@Override
			public ConnectionService addingService(ServiceReference<ConnectionService> reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null);
				return connectionService;
			}

			@Override
			public void removedService(ServiceReference<ConnectionService> reference, ConnectionService service)
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
		if (context.getParent().getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty())
			{
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				String title = "Mitgliedschaft entfernen";
				String msg = "Soll die ausgewählte Mitgliedschaft entfernt werden?";
				int type = MessageDialog.QUESTION;
				String[] buttons = new String[] { "Ja", "Nein" };
				MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
				if (dialog.open() == 0)
				{
					Iterator<?> iterator = ssel.iterator();
					while (iterator.hasNext())
					{
						Object object = iterator.next();
						if (object instanceof Membership)
						{
							if (connectionService != null)
							{
								MembershipQuery query = (MembershipQuery) connectionService.getQuery(Membership.class);
								object = query.delete((Membership) object);
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
