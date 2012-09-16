package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.views.PersonAddressGroupMemberView;
import ch.eugster.events.persistence.service.ConnectionService;

public class UpdatePersonAddressGroupMemberHandler extends AbstractHandler implements IHandler, IPropertyChangeListener
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	private PersonAddressGroupMemberView view;

	@Override
	public void dispose()
	{
		view.removePartPropertyListener(this);
		connectionServiceTracker.close();
		super.dispose();
	}

	public UpdatePersonAddressGroupMemberHandler()
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
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("activePart") instanceof PersonAddressGroupMemberView)
			{
				PersonAddressGroupMemberView view = (PersonAddressGroupMemberView) context.getParent().getVariable(
						"activePart");
				view.updateAddressGroupMembers();
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getParent().getVariable("activePart") instanceof PersonAddressGroupMemberView)
		{
			view = (PersonAddressGroupMemberView) context.getParent().getVariable("activePart");
			view.addPartPropertyListener(this);
			this.setBaseEnabled(view.isDirty());
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals("dirty"))
		{
			String dirty = (String) event.getNewValue();
			if (dirty != null)
			{
				setBaseEnabled(dirty.equals("true"));
			}
		}
	}

}
