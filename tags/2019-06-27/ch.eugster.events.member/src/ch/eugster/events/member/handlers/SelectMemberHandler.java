package ch.eugster.events.member.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.member.Activator;
import ch.eugster.events.member.dialog.MemberDialog;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.service.ConnectionService;

public class SelectMemberHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private ConnectionService connectionService;

	public SelectMemberHandler()
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
		this.addListenerObject(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			AbstractEntity entity = null;
			if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				entity = (LinkPersonAddress) ssel.getFirstElement();
			}
			else if (ssel.getFirstElement() instanceof Person)
			{
				entity = ((Person) ssel.getFirstElement()).getDefaultLink();
			}
			else if (ssel.getFirstElement() instanceof Address)
			{
				entity = (Address) ssel.getFirstElement();
			}
			if (entity instanceof LinkPersonAddress || entity instanceof Address)
			{
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				MemberDialog dialog = new MemberDialog(shell, entity);
				dialog.open();
			}
		}
		return null;
	}

}
