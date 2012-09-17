package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.persistence.model.SchoolClass;
import ch.eugster.events.visits.editors.SchoolClassEditor;
import ch.eugster.events.visits.editors.SchoolClassEditorInput;

public class NewSchoolClassHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		SchoolClass schoolClass = SchoolClass.newInstance();
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new SchoolClassEditorInput(schoolClass), SchoolClassEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
