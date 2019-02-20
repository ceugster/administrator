package ch.eugster.events.addresstype.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.addresstype.editors.AddressTypeEditor;
import ch.eugster.events.addresstype.editors.AddressTypeEditorInput;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class EditAddressTypeHandler extends ConnectionServiceDependentAbstractHandler
{
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty() && ssel.size() == 1)
			{
				if (ssel.getFirstElement() instanceof AddressType)
				{
					AddressType addressType = (AddressType) ssel.getFirstElement();
					AddressTypeEditorInput input = new AddressTypeEditorInput(addressType);
					try
					{
						IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
						window.getActivePage().openEditor(input, AddressTypeEditor.ID);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
}
