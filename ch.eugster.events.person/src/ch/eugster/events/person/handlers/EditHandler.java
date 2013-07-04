package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.person.editors.AddressEditor;
import ch.eugster.events.person.editors.AddressEditorInput;
import ch.eugster.events.person.editors.EditorSelector;

public class EditHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				if (ssel.size() == 1)
				{
					if (ssel.getFirstElement() instanceof AbstractEntity)
					{
						AbstractEntity entity = (AbstractEntity) ssel.getFirstElement();
						if (entity.isDeleted())
						{
							String title = null;
							String message = null;
							if (entity instanceof LinkPersonAddress || entity instanceof Person)
							{
								title = "Entfernte Person";
								message = "Eine entfernte Person kann nicht bearbeitet werden.";
							}
							else if (entity instanceof Address)
							{
								title = "Entfernte Adresse";
								message = "Eine entfernte Adresse kann nicht bearbeitet werden.";
							}
							Shell shell = (Shell) context.getParent().getVariable("activeShell");
							MessageDialog dialog = new MessageDialog(shell, title, null, message,
									MessageDialog.INFORMATION, new String[] { "OK" }, 0);
							dialog.open();
						}
						else
						{
							IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
							if (ssel.getFirstElement() instanceof Person)
							{
								Person person = (Person) ssel.getFirstElement();
								openPersonEditor(window, person.getDefaultLink());
							}
							else if (ssel.getFirstElement() instanceof LinkPersonAddress)
							{
								LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
								openPersonEditor(window, link.getPerson().getDefaultLink());
							}
							else if (ssel.getFirstElement() instanceof Address)
							{
								openAddressEditor(window, (Address) ssel.getFirstElement());
							}
						}
					}
				}
			}
		}
		return null;
	}

	private void openAddressEditor(final IWorkbenchWindow window, final Address address)
	{
		try
		{
			window.getActivePage().openEditor(new AddressEditorInput(address), AddressEditor.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	private void openPersonEditor(final IWorkbenchWindow window, final LinkPersonAddress link)
	{
		for (EditorSelector editorSelector : EditorSelector.values())
		{
			if (editorSelector.equals(EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]))
			{
				try
				{
					window.getActivePage()
							.openEditor(editorSelector.getEditorInput(link), editorSelector.getEditorId());
					break;
				}
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		if (evaluationContext != null)
		{
			EvaluationContext context = (EvaluationContext) evaluationContext;
			Object object = context.getParent().getVariable("selection");
			if (object instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) object;
				Object[] selected = ssel.toArray();
				for (Object sel : selected)
				{
					if (sel instanceof AbstractEntity)
					{
						if (!((AbstractEntity) sel).isDeleted())
						{
							super.setBaseEnabled(true);
							return;
						}
					}
				}
			}
		}
		super.setBaseEnabled(false);
	}
}
