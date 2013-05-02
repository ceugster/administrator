package ch.eugster.events.person.handlers;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.EditorSelector;
import ch.eugster.events.person.views.PersonView;

public class AddPersonHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
				Collection<AddressType> addressTypes = query.selectAll(false);
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
		}
		finally
		{
			tracker.close();
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
					view.getSearcher().fillPerson(person);
				}
			}
		}

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
}
