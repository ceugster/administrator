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

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class ReactivateHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public ReactivateHandler()
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
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				if (ssel.getFirstElement() instanceof Person || ssel.getFirstElement() instanceof Address)
				{
					Shell shell = (Shell) context.getParent().getVariable("activeShell");
					String title = "Objekt wiederherstellen";
					String msg = "Soll das ausgewählte Objekt wiederhergestellt werden?";
					int type = MessageDialog.QUESTION;
					String[] buttons = new String[] { "Ja", "Nein" };
					MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
					if (dialog.open() == 0)
					{
						Iterator<?> iterator = ssel.iterator();
						while (iterator.hasNext())
						{
							Object object = iterator.next();
							if (object instanceof Person)
							{
								Person person = (Person) object;
								if (person.isDeleted())
								{
									if (connectionService != null)
									{
										PersonQuery query = (PersonQuery) connectionService.getQuery(Person.class);
										person.setDeleted(false);
										person = query.merge(person);
									}
								}
							}
							else if (object instanceof Address)
							{
								Address address = (Address) object;
								if (address.isDeleted())
								{
									if (connectionService != null)
									{
										AddressQuery query = (AddressQuery) connectionService.getQuery(Address.class);
										address.setDeleted(false);
										address = query.merge(address);
									}
								}
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
		if (evaluationContext != null)
		{
			EvaluationContext context = (EvaluationContext) evaluationContext;
			Object object = context.getParent().getVariable("selection");
			if (object instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) object;
				if (ssel.getFirstElement() instanceof Person)
				{
					if (((Person) ssel.getFirstElement()).isDeleted())
					{
						super.setBaseEnabled(true);
						return;
					}
				}
				else if (ssel.getFirstElement() instanceof Address)
				{
					if (((Address) ssel.getFirstElement()).isDeleted())
					{
						super.setBaseEnabled(true);
						return;
					}
				}
			}
		}
		super.setBaseEnabled(false);
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();
	}
}
