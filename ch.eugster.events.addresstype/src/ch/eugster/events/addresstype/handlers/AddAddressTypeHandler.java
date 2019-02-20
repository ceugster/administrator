package ch.eugster.events.addresstype.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.addresstype.editors.AddressTypeEditor;
import ch.eugster.events.addresstype.editors.AddressTypeEditorInput;
import ch.eugster.events.persistence.model.AddressType;

public class AddAddressTypeHandler extends AbstractHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		AddressType addressType = AddressType.newInstance();
		AddressTypeEditorInput input = new AddressTypeEditorInput(addressType);
		try
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				IWorkbenchWindow window = (IWorkbenchWindow) context.getParent().getVariable("activeWorkbenchWindow");
				window.getActivePage().openEditor(input, AddressTypeEditor.ID);
			}
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		// if (User.getCurrent() instanceof User)
		// {
		// setBaseEnabled(User.getCurrent().getState().equals(User.UserStatus.ADMINISTRATOR));
		// }
		// else
		// {
		// setBaseEnabled(false);
		// }
		setBaseEnabled(true);
	}
}
