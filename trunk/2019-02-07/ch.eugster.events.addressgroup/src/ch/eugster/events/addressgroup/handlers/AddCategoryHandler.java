package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.addressgroup.editors.AddressGroupCategoryEditor;
import ch.eugster.events.addressgroup.editors.AddressGroupCategoryEditorInput;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.Domain;

public class AddCategoryHandler extends AbstractHandler implements IHandler
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
				if (view.getViewer().getInput() instanceof Domain)
				{
					AddressGroupCategory category = AddressGroupCategory.newInstance((Domain) view.getViewer().getInput());
					AddressGroupCategoryEditorInput input = new AddressGroupCategoryEditorInput(category);
					try
					{
						IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
						window.getActivePage().openEditor(input, AddressGroupCategoryEditor.ID);
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
