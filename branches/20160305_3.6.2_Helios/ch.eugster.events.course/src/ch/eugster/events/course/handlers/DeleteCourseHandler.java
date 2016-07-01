package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.queries.CourseQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DeleteCourseHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteCourseHandler()
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
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
				IWorkbenchPart part = (IWorkbenchPart) context.getVariable("activePart");
				if (!ssel.isEmpty() && ssel.size() == 1)
				{
					if (ssel.getFirstElement() instanceof Course)
					{
						Course course = (Course) ssel.getFirstElement();
						Shell shell = part.getSite().getShell();
						String title = "L�schbest�tigung";
						StringBuilder msg = new StringBuilder("Soll der ausgew�hlte Kurs ");
						msg = msg.append(course.getCode().equals("") ? course.getTitle() : course.getCode() + " - "
								+ course.getTitle());
						msg = msg.append(" entfernt werden?");
						int icon = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons, 0);
						if (dialog.open() == 0)
						{
							if (connectionService != null)
							{
								CourseQuery query = (CourseQuery) connectionService.getQuery(Course.class);
								course = query.delete(course);
							}
						}
					}
				}
			}
		}
		return null;
	}
}