package ch.eugster.events.season.handlers;

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

import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.SeasonQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.season.Activator;

public class DeleteSeasonHandler extends AbstractHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteSeasonHandler()
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
					if (ssel.getFirstElement() instanceof Season)
					{
						Season season = (Season) ssel.getFirstElement();
						if (season.isClosed())
						{
							Shell shell = part.getSite().getShell();
							String title = "Löschbestätigung";
							StringBuilder msg = new StringBuilder("Soll die ausgewählte Saison ");
							msg = msg.append(season.getCode().equals("") ? season.getTitle() : season.getCode() + " - "
									+ season.getTitle());
							msg = msg.append(" entfernt werden?");
							msg = msg
									.append("\nBitte beachten Sie: Es werden auch alle Veranstaltungen dieser Saison entfernt!");
							int icon = MessageDialog.QUESTION;
							String[] buttons = new String[] { "Ja", "Nein" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons,
									0);
							if (dialog.open() == 0)
							{
								if (connectionService != null)
								{
									SeasonQuery query = (SeasonQuery) connectionService.getQuery(Season.class);
									season = query.delete(season);
								}
							}
						}
						else
						{
							Shell shell = part.getSite().getShell();
							String title = "Löschmeldung";
							StringBuilder msg = new StringBuilder("Die ausgewählte Saison ");
							msg = msg.append(season.getCode().equals("") ? season.getTitle() : season.getCode() + " - "
									+ season.getTitle());
							msg = msg.append(" ist aktiv und kann deshalb nicht entfernt werden.");
							msg = msg.append(" Es können nur abgeschlossene Saisons entfernt werden.");
							int icon = MessageDialog.INFORMATION;
							String[] buttons = new String[] { "OK" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons,
									0);
							dialog.open();
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
