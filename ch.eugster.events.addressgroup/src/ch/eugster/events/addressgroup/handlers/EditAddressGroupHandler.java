package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.addressgroup.editors.AddressGroupEditor;
import ch.eugster.events.addressgroup.editors.AddressGroupEditorInput;
import ch.eugster.events.persistence.model.AddressGroup;

public class EditAddressGroupHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
			if (!ssel.isEmpty() && ssel.size() == 1)
			{
				if (ssel.getFirstElement() instanceof AddressGroup)
				{
					AddressGroup addressGroup = (AddressGroup) ssel.getFirstElement();
					AddressGroupEditorInput input = new AddressGroupEditorInput(addressGroup);
					try
					{
						IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
						window.getActivePage().openEditor(input, AddressGroupEditor.ID);
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
