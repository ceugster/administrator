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
import org.eclipse.ui.views.contentoutline.ContentOutline;

import ch.eugster.events.course.views.CourseEditorContentOutlinePage;
import ch.eugster.events.course.views.CourseEditorContentOutlinePage.CourseDetailGroup;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.model.CourseDetail;

public class DeleteCourseDetailHandler extends AbstractHandler implements IHandler
{

	private void delete(final CourseDetail courseDetail, final CourseEditorContentOutlinePage page)
	{
		courseDetail.getPropertyChangeSupport().addPropertyChangeListener(page);
		courseDetail.setDeleted(true);
		courseDetail.getPropertyChangeSupport().removePropertyChangeListener(page);
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		IWorkbenchPart activePart = (IWorkbenchPart) context.getParent().getVariable("activePart");
		if (activePart instanceof ContentOutline)
		{
			ContentOutline contentOutline = (ContentOutline) activePart;
			if (contentOutline.getCurrentPage() instanceof CourseEditorContentOutlinePage)
			{
				CourseEditorContentOutlinePage page = (CourseEditorContentOutlinePage) contentOutline.getCurrentPage();
				if (context.getParent().getVariable("selection") instanceof StructuredSelection)
				{
					StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
					if (!ssel.isEmpty())
					{
						if (ssel.getFirstElement() instanceof CourseDetail)
						{
							CourseDetail courseDetail = (CourseDetail) ssel.getFirstElement();
							String detail = CourseFormatter.getInstance().formatComboEntry(courseDetail);
							Shell shell = (Shell) context.getParent().getVariable("activeShell");
							String title = "Kursdaten entfernen";
							String msg = "Sollen die Kursdaten " + detail + " entfernt werden?";
							int type = MessageDialog.QUESTION;
							String[] buttons = new String[] { "Ja", "Nein" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
							if (dialog.open() == 0)
							{
								this.delete(courseDetail, page);
							}
						}
						else if (ssel.getFirstElement() instanceof CourseDetailGroup)
						{
							CourseDetailGroup courseDetailGroup = (CourseDetailGroup) ssel.getFirstElement();
							String course = CourseFormatter.getInstance().formatComboEntry(
									courseDetailGroup.getRoot().getCourse());
							Shell shell = (Shell) context.getParent().getVariable("activeShell");
							String title = "Kursdaten entfernen";
							String msg = "Sollen die Kursdaten zu '" + course + "' entfernt werden?";
							int type = MessageDialog.QUESTION;
							String[] buttons = new String[] { "Ja", "Nein" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
							if (dialog.open() == 0)
							{
								for (CourseDetail courseDetail : courseDetailGroup.getCourseDetails())
								{
									this.delete(courseDetail, page);
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

}
