package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.visits.editors.ApplianceEditor;
import ch.eugster.events.visits.editors.ApplianceEditorInput;

public class NewApplianceHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Appliance appliance = Appliance.newInstance();
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new ApplianceEditorInput(appliance), ApplianceEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
