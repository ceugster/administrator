package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.editors.BookingEditor;
import ch.eugster.events.course.editors.BookingEditorInput;
import ch.eugster.events.persistence.model.Booking;

public class EditBookingHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
		if (ssel.size() == 1 && ssel.getFirstElement() instanceof Booking)
		{
			Booking booking = (Booking) ssel.getFirstElement();
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new BookingEditorInput(booking), BookingEditor.ID);
			} 
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

}
