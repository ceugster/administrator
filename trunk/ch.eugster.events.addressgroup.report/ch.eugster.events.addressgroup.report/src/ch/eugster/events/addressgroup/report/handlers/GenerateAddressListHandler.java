package ch.eugster.events.addressgroup.report.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.addressgroup.report.dialogs.AddressListDialog;
import ch.eugster.events.documents.services.DocumentBuilderService;

public class GenerateAddressListHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				AddressListDialog dialog = new AddressListDialog(shell, ssel);
				dialog.open();
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class.getName(), null);
		try
		{
			tracker.open();
			ServiceReference[] references = tracker.getServiceReferences();
			if (references != null)
			{
				for (ServiceReference reference : references)
				{
					String target = (String) reference.getProperty("target");
					if (target != null && target.equals("spreadsheet.address.list"))
					{
						enabled = true;
						break;
					}
				}
			}
		}
		finally
		{
			tracker.close();
		}
		this.setBaseEnabled(enabled);
	}

}
