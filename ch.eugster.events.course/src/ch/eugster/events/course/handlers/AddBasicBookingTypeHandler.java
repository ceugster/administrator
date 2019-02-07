package ch.eugster.events.course.handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.editors.BasicBookingTypeEditor;
import ch.eugster.events.course.editors.BasicBookingTypeEditorInput;
import ch.eugster.events.persistence.model.BookingTypeProposition;


public class AddBasicBookingTypeHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new BasicBookingTypeEditorInput(BookingTypeProposition.newInstance()), BasicBookingTypeEditor.ID);
		} 
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
