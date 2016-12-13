package ch.eugster.events.visits.handlers;

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

import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.queries.SchoolLevelQuery;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.Activator;

public class DeleteSchoolLevelHandler extends AbstractHandler
{
	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteSchoolLevelHandler()
	{
		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
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
				super.removedService(reference, service);
				setBaseEnabled(false);
			}
		};
		connectionServiceTracker.open();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object evaluationContext = event.getApplicationContext();
		if (evaluationContext instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			Object selection = context.getParent().getVariable("selection");
			if (selection instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) selection;
				if (ssel.getFirstElement() instanceof Appliance)
				{
					SchoolLevel schoolLevel = (SchoolLevel) ssel.getFirstElement();
					if (connectionService != null)
					{
						IWorkbenchPart part = (IWorkbenchPart) context.getParent().getVariable("activePart");
						Shell shell = part.getSite().getShell();

						VisitQuery visitQuery = (VisitQuery) connectionService.getQuery(Visit.class);
						if (visitQuery.countSchoolLevels(schoolLevel) == 0L)
						{
							String title = "Löschbestätigung";
							StringBuilder msg = new StringBuilder("Soll die ausgewählte Schulklasse "
									+ schoolLevel.getName() + " entfernt werden?");
							int icon = MessageDialog.QUESTION;
							String[] buttons = new String[] { "Ja", "Nein" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons,
									0);
							if (dialog.open() == 0)
							{
								if (connectionService != null)
								{
									SchoolLevelQuery deleteQuery = (SchoolLevelQuery) connectionService
											.getQuery(SchoolLevel.class);
									deleteQuery.delete(schoolLevel);
								}
							}
						}
						else
						{
							String title = schoolLevel.getName();
							StringBuilder msg = new StringBuilder("Die ausgewählte Schulklasse "
									+ schoolLevel.getName() + " kann nicht entfernt werden, da sie in Verwendung ist.");
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
