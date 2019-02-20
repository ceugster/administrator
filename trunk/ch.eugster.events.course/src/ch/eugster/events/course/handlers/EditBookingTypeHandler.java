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
import ch.eugster.events.course.views.CourseEditorContentOutlinePage.BookingTypeGroup;
import ch.eugster.events.course.wizards.BookingTypeWizard;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.ui.wizards.WizardDialog;

public class EditBookingTypeHandler extends AbstractHandler implements IHandler
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
					if (!ssel.isEmpty() && ssel.getFirstElement() instanceof BookingType)
					{
						BookingType bookingType = (BookingType) ssel.getFirstElement();
						bookingType.getPropertyChangeSupport().addPropertyChangeListener(page);
						BookingTypeGroup bookingTypeGroup = page.getRoot().getBookingTypeGroup();
						BookingTypeWizard wizard = new BookingTypeWizard(bookingTypeGroup, bookingType);
						Shell shell = (Shell) context.getParent().getVariable("activeShell");
						WizardDialog dialog = new WizardDialog(shell, wizard);
						dialog.open();
						bookingType.getPropertyChangeSupport().removePropertyChangeListener(page);
					}
				}
			}
		}
		return null;
	}

}
