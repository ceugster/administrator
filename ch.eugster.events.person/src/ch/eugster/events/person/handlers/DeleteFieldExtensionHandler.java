package ch.eugster.events.person.handlers;

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

import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class DeleteFieldExtensionHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteFieldExtensionHandler()
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
		if (context.getParent().getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty())
			{
				if (ssel.getFirstElement() instanceof FieldExtension)
				{
					Shell shell = (Shell) context.getParent().getVariable("activeShell");
					String title = "Zusatzfeld entfernen";

					String msg = "Soll das ausgewählte Zusatzfeld entfernt werden?";
					int type = MessageDialog.QUESTION;
					String[] buttons = new String[] { "Ja", "Nein" };
					MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
					if (dialog.open() == 0)
					{
						Iterator<?> iterator = ssel.iterator();
						while (iterator.hasNext())
						{
							Object object = iterator.next();
							if (object instanceof AddressSalutation)
							{
								if (connectionService != null)
								{
									FieldExtensionQuery deleteQuery = (FieldExtensionQuery) connectionService
											.getQuery(FieldExtension.class);
									object = deleteQuery.delete((FieldExtension) object);
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
}
