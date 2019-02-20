package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import ch.eugster.events.course.views.CourseEditorContentOutlinePage;
import ch.eugster.events.course.views.CourseEditorContentOutlinePage.CourseDetailGroup;
import ch.eugster.events.course.wizards.CourseDetailWizard;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.ui.wizards.WizardDialog;

public class EditCourseDetailHandler extends AbstractHandler implements
IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
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
					if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseDetail)
					{
						CourseDetail courseDetail = (CourseDetail) ssel.getFirstElement();
						courseDetail.getPropertyChangeSupport().addPropertyChangeListener(page);
						CourseDetailGroup courseDetailGroup = page.getRoot().getCourseDetailGroup();
						CourseDetailWizard wizard = new CourseDetailWizard(courseDetailGroup, courseDetail);
						Shell shell = (Shell) context.getParent().getVariable("activeShell");
						WizardDialog dialog = new WizardDialog(shell, wizard);
						dialog.open();
						courseDetail.getPropertyChangeSupport().removePropertyChangeListener(page);
					}
				}
			}
		}
		return null;
	}
}
