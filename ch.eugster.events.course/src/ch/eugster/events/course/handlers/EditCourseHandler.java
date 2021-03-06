package ch.eugster.events.course.handlers;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.editors.CourseEditor;
import ch.eugster.events.course.editors.CourseEditorInput;
import ch.eugster.events.course.views.CourseView;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;


public class EditCourseHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof CourseView)
		{
			CourseView view = (CourseView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Course)
			{
				Course course = (Course) ssel.getFirstElement();
				try
				{
					if (connectionService != null)
					{
						course = (Course) connectionService.refresh(course);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new CourseEditorInput(course), CourseEditor.ID);
					}
				} 
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
