package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.visits.editors.EmailAccountEditor;
import ch.eugster.events.visits.editors.EmailAccountEditorInput;

public class AddEmailAccountHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EmailAccount account = EmailAccount.newInstance();
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new EmailAccountEditorInput(account), EmailAccountEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
