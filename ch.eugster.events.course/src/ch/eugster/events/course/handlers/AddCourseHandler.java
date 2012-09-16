package ch.eugster.events.course.handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.editors.CourseEditor;
import ch.eugster.events.course.editors.CourseEditorInput;
import ch.eugster.events.course.views.CourseView;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Season;


public class AddCourseHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof CourseView)
		{
			CourseView view = (CourseView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Season)
			{
				Season season = (Season) ssel.getFirstElement();

				try
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new CourseEditorInput(Course.newInstance(season)), CourseEditor.ID);
				} catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
