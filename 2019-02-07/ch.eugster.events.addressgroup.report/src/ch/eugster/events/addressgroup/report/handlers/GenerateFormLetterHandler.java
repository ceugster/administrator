package ch.eugster.events.addressgroup.report.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.addressgroup.report.dialogs.FormLetterDialog;
import ch.eugster.events.documents.services.DocumentBuilderService;

public class GenerateFormLetterHandler extends AbstractHandler implements IHandler
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
				FormLetterDialog dialog = new FormLetterDialog(shell, ssel);
				dialog.open();
			}
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class, null);
		tracker.open();
		try
		{
			setBaseEnabled(tracker.getService() != null);
		}
		finally
		{
			tracker.close();
		}
	}
}
