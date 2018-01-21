package ch.eugster.events.charity.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.charity.editors.CharityRunEditor;
import ch.eugster.events.charity.editors.CharityRunEditorInput;
import ch.eugster.events.persistence.model.CharityRun;

public class AddCharityRunHandler  extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try
		{
			CharityRun charityRun = CharityRun.newInstance();
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new CharityRunEditorInput(charityRun), CharityRunEditor.ID);
		} 
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return IStatus.OK;
	}

}
