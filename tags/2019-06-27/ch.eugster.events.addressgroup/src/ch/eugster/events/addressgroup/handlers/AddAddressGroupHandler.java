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
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;

public class AddAddressGroupHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			Object object = context.getVariable("activePart");
			if (object instanceof AddressGroupView)
			{
				AddressGroupView view = (AddressGroupView) object;
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (ssel.size() == 1 && ssel.getFirstElement() instanceof AddressGroupCategory)
				{
					AddressGroupCategory category = (AddressGroupCategory) ssel.getFirstElement();
					AddressGroup group = AddressGroup.newInstance(category);
					AddressGroupEditorInput input = new AddressGroupEditorInput(group);
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

	@Override
	public void setEnabled(Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context instanceof EvaluationContext)
		{
			Object object = context.getVariable("activePart");
			if (object instanceof AddressGroupView)
			{
				AddressGroupView view = (AddressGroupView) object;
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (ssel.size() == 1 && ssel.getFirstElement() instanceof AddressGroupCategory)
				{
					enabled = true;
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
