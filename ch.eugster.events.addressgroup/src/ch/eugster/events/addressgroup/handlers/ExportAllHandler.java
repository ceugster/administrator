package ch.eugster.events.addressgroup.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.service.ConnectionService;

public class ExportAllHandler extends AbstractHandler implements IHandler
{
	private DocumentBuilderService documentBuilderService;
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			final EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			final Shell shell = (Shell) context.getParent().getVariable("shell");

	        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
	        try 
	        {
				dialog.run(true, true, new RunnableWithProgress(event, shell));
				dialog.close();
			} 
	        catch (InvocationTargetException e) 
	        {
	        	e.printStackTrace();
				MessageDialog.openInformation(shell, "Es ist ein Fehler aufgetreten", e.getLocalizedMessage());
			} 
	        catch (InterruptedException e) 
	        {
				e.printStackTrace();
			}
		}
		return new Object();
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = this.documentBuilderService != null;
		if (!enabled)
		{
			ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
					DocumentBuilderService.class, null);
			try
			{
				tracker.open();
				ServiceReference<DocumentBuilderService>[] references = tracker.getServiceReferences();
				if (references != null)
				{
					for (ServiceReference<DocumentBuilderService> reference : references)
					{
						String target = (String) reference.getProperty("target");
						if (target != null && target.equals("spreadsheet.address.list"))
						{
							enabled = true;
							this.documentBuilderService = tracker.getService(reference);
							break;
						}
					}
				}
			}
			finally
			{
				tracker.close();
			}
		}
		this.setBaseEnabled(enabled);
	}

	public class RunnableWithProgress implements IRunnableWithProgress
	{
		private Shell shell;
		
		private EvaluationContext context;
		
		private ConnectionService connectionService;
		
		public RunnableWithProgress(ExecutionEvent event, Shell shell)
		{
			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				this.context = (EvaluationContext) event.getApplicationContext();
				if (context.getParent().getVariable("activePart") != null)
				{
					final AddressGroupView view = (AddressGroupView) context.getParent().getVariable("activePart");
					this.connectionService = (ConnectionService) view.getViewer().getInput();
				}
			}
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException 
		{
			documentBuilderService.buildDocument(monitor, connectionService, shell);
		}
	}
}
