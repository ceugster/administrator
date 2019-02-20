package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

import ch.eugster.events.course.views.CourseView;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.helpers.ClipboardHelper;

public class CourseCopyHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getVariable("activePart") instanceof CourseView)
			{
				CourseView view = (CourseView) context.getVariable("activePart");
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				Object[] objects = ssel.toArray();
				for (Object object : objects)
				{
					if (!Course.class.isInstance(object))
					{
						return Status.OK_STATUS;
					}
				}
				CourseTransfer transfer = CourseTransfer.getTransfer();
				Object[] selection = ssel.toArray();
				transfer.setData(DND.DROP_COPY, selection);
				ClipboardHelper.getClipboard().setContents(new Object[] { selection }, new Transfer[] { transfer });
			}
		}
		return Status.OK_STATUS;
	}

}
