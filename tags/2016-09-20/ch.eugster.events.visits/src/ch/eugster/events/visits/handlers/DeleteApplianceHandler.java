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
import ch.eugster.events.persistence.model.VisitAppliance;
import ch.eugster.events.persistence.queries.ApplianceQuery;
import ch.eugster.events.persistence.queries.VisitApplianceQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.Activator;

public class DeleteApplianceHandler extends AbstractHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteApplianceHandler()
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
					Appliance appliance = (Appliance) ssel.getFirstElement();
					if (connectionService != null)
					{
						IWorkbenchPart part = (IWorkbenchPart) context.getParent().getVariable("activePart");
						Shell shell = part.getSite().getShell();

						VisitApplianceQuery visitApplianceQuery = (VisitApplianceQuery) connectionService
								.getQuery(VisitAppliance.class);
						if (visitApplianceQuery.count() == 0L)
						{
							String title = "Löschbestätigung";
							StringBuilder msg = new StringBuilder("Soll das ausgewählte Gerät " + appliance.getName()
									+ " entfernt werden?");
							int icon = MessageDialog.QUESTION;
							String[] buttons = new String[] { "Ja", "Nein" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons,
									0);
							if (dialog.open() == 0)
							{
								if (connectionService != null)
								{
									ApplianceQuery deleteQuery = (ApplianceQuery) connectionService
											.getQuery(Appliance.class);
									deleteQuery.delete(appliance);
								}
							}
						}
						else
						{
							String title = appliance.getName();
							StringBuilder msg = new StringBuilder("Das ausgewählte Gerät " + appliance.getName()
									+ " kann nicht entfernt werden, da in Verwendung ist.");
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
