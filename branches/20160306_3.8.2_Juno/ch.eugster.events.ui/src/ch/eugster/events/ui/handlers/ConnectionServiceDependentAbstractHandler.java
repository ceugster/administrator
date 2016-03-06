package ch.eugster.events.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.IHandler;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.Activator;

public abstract class ConnectionServiceDependentAbstractHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	protected ConnectionService connectionService;

	public ConnectionServiceDependentAbstractHandler()
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
	public void setEnabled(Object evaluationContext)
	{
		setBaseEnabled(connectionService != null);
	}
}
