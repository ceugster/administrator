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
import ch.eugster.events.course.views.CourseEditorContentOutlinePage.BookingTypeGroup;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.model.BookingType;

public class DeleteBookingTypeHandler extends AbstractHandler implements IHandler
{

	private void delete(final BookingType bookingType, final CourseEditorContentOutlinePage page)
	{
		bookingType.getPropertyChangeSupport().addPropertyChangeListener(page);
		bookingType.setDeleted(true);
		bookingType.getPropertyChangeSupport().removePropertyChangeListener(page);
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
						if (ssel.getFirstElement() instanceof BookingType)
						{
							BookingType bookingType = (BookingType) ssel.getFirstElement();
							String typename = CourseFormatter.getInstance().formatComboEntry(bookingType);
							Shell shell = (Shell) context.getParent().getVariable("activeShell");
							String title = "Buchungsart entfernen";
							String msg = "Soll die Buchungsart " + typename + " entfernt werden?";
							int type = MessageDialog.QUESTION;
							String[] buttons = new String[] { "Ja", "Nein" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
							if (dialog.open() == 0)
							{
								this.delete(bookingType, page);
							}
						}
						else if (ssel.getFirstElement() instanceof BookingTypeGroup)
						{
							BookingTypeGroup bookingTypeGroup = (BookingTypeGroup) ssel.getFirstElement();
							String course = CourseFormatter.getInstance().formatComboEntry(
									bookingTypeGroup.getRoot().getCourse());
							Shell shell = (Shell) context.getParent().getVariable("activeShell");
							String title = "Buchungsarten entfernen";
							String msg = "Sollen die Buchungsarten zu '" + course + "' entfernt werden?";
							int type = MessageDialog.QUESTION;
							String[] buttons = new String[] { "Ja", "Nein" };
							MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
							if (dialog.open() == 0)
							{
								for (BookingType bookingType : bookingTypeGroup.getBookingTypes())
								{
									this.delete(bookingType, page);
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
