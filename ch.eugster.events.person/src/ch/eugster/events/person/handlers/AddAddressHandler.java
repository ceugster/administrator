package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.person.editors.AddressEditor;
import ch.eugster.events.person.editors.AddressEditorInput;
import ch.eugster.events.person.views.PersonView;

public class AddAddressHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		try
		{
			Address address = Address.newInstance();
			IWorkbenchWindow window = (IWorkbenchWindow) context.getParent().getVariable("activeWorkbenchWindow");
			IViewReference[] references = window.getActivePage().getViewReferences();
			for (IViewReference reference : references)
			{
				if (reference.getId().equals(PersonView.ID))
				{
					IViewPart part = reference.getView(false);
					if (part instanceof PersonView)
					{
						PersonView view = (PersonView) part;
						view.getViewer().setSelection(new StructuredSelection());
						view.getSearcher().fillAddress(address);
					}
				}
			}
			window.getActivePage().openEditor(new AddressEditorInput(address), AddressEditor.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
