package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.dialogs.AddressGroupMemberDialog;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class EditAddressGroupMembers extends AbstractHandler implements IHandler
{
	private ServiceTracker tracker;

	private ConnectionService connectionService;

	public EditAddressGroupMembers()
	{
		tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		connectionService = (ConnectionService) tracker.getService();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) context.getParent().getVariable("activeWorkbenchWindowShell");
		StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("activeMenuSelection");
		if (!ssel.isEmpty())
		{
			LinkPersonAddress link = null;
			Address address = null;
			if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				link = refresh((LinkPersonAddress) ssel.getFirstElement());
			}
			else if (ssel.getFirstElement() instanceof Person)
			{
				Person person = (Person) ssel.getFirstElement();
				link = refresh(person.getDefaultLink());
			}
			else if (ssel.getFirstElement() instanceof Address)
			{
				address = refresh((Address) ssel.getFirstElement());
			}
			else if (ssel.getFirstElement() instanceof AddressGroupMember)
			{
				AddressGroupMember member = ((AddressGroupMember) ssel.getFirstElement());
				link = refresh(member.getLink());
				if (link == null)
				{
					address = refresh(member.getAddress());
				}

			}
			AddressGroupMemberDialog dialog = null;
			if (link != null)
			{
				dialog = new AddressGroupMemberDialog(shell, link);
			}
			else if (address != null)
			{
				dialog = new AddressGroupMemberDialog(shell, address);
			}
			if (dialog != null)
			{
				dialog.open();
			}
		}
		return null;
	}

	private Person refresh(Person person)
	{
		ConnectionService service = (ConnectionService) tracker.getService();
		try
		{
			PersonQuery query = (PersonQuery) service.getQuery(Person.class);
			return (Person) query.refresh(person);
		}
		catch (Exception e)
		{
			PersonQuery query = (PersonQuery) service.getQuery(Person.class);
			return query.find(Person.class, person.getId());
		}
	}

	private Address refresh(Address address)
	{
		ConnectionService service = (ConnectionService) tracker.getService();
		try
		{
			AddressQuery query = (AddressQuery) service.getQuery(Address.class);
			return (Address) query.refresh(address);
		}
		catch (Exception e)
		{
			AddressQuery query = (AddressQuery) service.getQuery(Address.class);
			return query.find(Address.class, address.getId());
		}
	}

	private LinkPersonAddress refresh(LinkPersonAddress link)
	{
		ConnectionService service = (ConnectionService) tracker.getService();
		try
		{
			LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
			link = (LinkPersonAddress) query.refresh(link);
			return link;
		}
		catch (Exception e)
		{
			LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
			return query.find(LinkPersonAddress.class, link.getId());
		}
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
			if (ssel != null && !ssel.isEmpty())
			{
				Object object = ssel.getFirstElement();
				if (object instanceof LinkPersonAddress || object instanceof Address
						|| object instanceof AddressGroupMember || object instanceof Person)
				{
					enabled = true;
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
