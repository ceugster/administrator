package ch.eugster.events.course.reporting.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.course.reporting.dialogs.ParticipationConfirmationDialog;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;

public class GenerateParticipationConfirmationHandler extends AbstractHandler implements IHandler
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
				ParticipationConfirmationDialog dialog = new ParticipationConfirmationDialog(shell, ssel);
				dialog.open();
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object object)
	{
		boolean enabled = false;
		ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class, null);
		tracker.open();
		try
		{
			enabled = tracker.getServiceReferences().length > 0;
			if (enabled)
			{
				if (object instanceof EvaluationContext)
				{
					EvaluationContext context = (EvaluationContext) object;
					if (context.getParent().getVariable("selection") instanceof StructuredSelection)
					{
						StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
						Object[] elements = ssel.toArray();
						for (Object element : elements)
						{
							if (element instanceof Course)
							{

							}
							else if (element instanceof Booking)
							{

							}
							else
							{
								enabled = false;
								break;
							}
						}
					}
				}
			}
		}
		finally
		{
			tracker.close();
		}
		setBaseEnabled(enabled);
	}
}
