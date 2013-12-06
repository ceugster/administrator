package ch.eugster.events.addresstype.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addresstype.Activator;
import ch.eugster.events.addresstype.editors.AddressTypeEditor;
import ch.eugster.events.addresstype.editors.AddressTypeEditorInput;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.service.ConnectionService;

public class EditAddressTypeHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public EditAddressTypeHandler()
	{
		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null && User.isCurrentUserAdministrator());
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
	// enabled = ssel.size() == 1 && ssel.getFirstElement() instanceof
	// AddressType;
	// }
	// }
	// }
	// setBaseEnabled(enabled);
	// }

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty() && ssel.size() == 1)
			{
				if (ssel.getFirstElement() instanceof AddressType)
				{
					AddressType addressType = (AddressType) ssel.getFirstElement();
					AddressTypeEditorInput input = new AddressTypeEditorInput(addressType);
					try
					{
						IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
						window.getActivePage().openEditor(input, AddressTypeEditor.ID);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
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
