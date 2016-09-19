package ch.eugster.events.person.handlers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.person.editors.EditorSelector;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class AddPersonHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (connectionService != null)
		{
			AddressTypeQuery query = (AddressTypeQuery) connectionService.getQuery(AddressType.class);
			List<AddressType> addressTypes = query.selectAll(false);
			if (addressTypes.isEmpty())
			{
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				MessageDialog
						.openWarning(shell, "Kein Adresstyp vorhanden",
								"Bevor Sie Adressen und Personen erfassen können, muss mindestens ein Adresstyp vorhanden sein.");
			}
			else
			{
				showEditor(context, addressTypes.iterator().next());
			}

		}
		return Status.OK_STATUS;
	}

	private void showEditor(final EvaluationContext context, final AddressType addressType)
	{
		Person person = Person.newInstance();
		person.setCountry(GlobalSettings.getInstance().getCountry());
		Address address = Address.newInstance();
		address.setCountry(GlobalSettings.getInstance().getCountry());
		LinkPersonAddress link = LinkPersonAddress.newInstance(person, address);
		link.setAddressType(addressType);
		// person.setDefaultLink(link);
		// person.addLink(link);

		Map<String, String> initialValues = null;
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
					initialValues = view.getSearcher().getInitialValues();
				}
			}
		}

		for (EditorSelector editorSelector : EditorSelector.values())
		{
			if (editorSelector.equals(EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]))
			{
				try
				{
					window.getActivePage().openEditor(editorSelector.getEditorInput(link, initialValues),
							editorSelector.getEditorId());
					break;
				}
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
