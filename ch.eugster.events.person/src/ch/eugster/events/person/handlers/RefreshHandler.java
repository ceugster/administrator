package ch.eugster.events.person.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.views.PersonView;

public class RefreshHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker tracker;

	private ConnectionService connectionService;

	@Override
	public void dispose()
	{
		tracker.close();
		super.dispose();
	}

	public RefreshHandler()
	{
		tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		connectionService = (ConnectionService) tracker.getService();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			Object sel = context.getVariable("selection");
			Object part = context.getVariable("activePart");
			if (sel instanceof IStructuredSelection)
			{
				IStructuredSelection ssel = (IStructuredSelection) sel;
				Iterator<?> iterator = ssel.iterator();
				while (iterator.hasNext())
				{
					Object entity = iterator.next();
					if (entity instanceof Person)
					{
						Person person = refresh((Person) entity);
						updateViewer(part, person);
					}
					else if (entity instanceof Address)
					{
						Address address = refresh((Address) entity);
						updateViewer(part, address);
					}
					else if (entity instanceof LinkPersonAddress)
					{
						LinkPersonAddress link = refresh((LinkPersonAddress) entity);
						updateViewer(part, link);
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private void updateViewer(Object part, AbstractEntity entity)
	{
		if (connectionService != null)
		{
			if (part instanceof PersonView)
			{
				PersonView view = (PersonView) part;
				view.getViewer().refresh(entity);
			}
		}
	}

	private Person refresh(Person person)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			PersonQuery query = (PersonQuery) service.getQuery(Person.class);
			return (Person) query.refresh(person);
		}
		catch (Exception e)
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			PersonQuery query = (PersonQuery) service.getQuery(Person.class);
			return query.find(Person.class, person.getId());
		}
		finally
		{
			tracker.close();
		}
	}

	private Address refresh(Address address)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			AddressQuery query = (AddressQuery) service.getQuery(Address.class);
			return (Address) query.refresh(address);
		}
		catch (Exception e)
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			AddressQuery query = (AddressQuery) service.getQuery(Address.class);
			return query.find(Address.class, address.getId());
		}
		finally
		{
			tracker.close();
		}
	}

	private LinkPersonAddress refresh(LinkPersonAddress link)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
			return (LinkPersonAddress) query.refresh(link);
		}
		catch (Exception e)
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
			return query.find(LinkPersonAddress.class, link.getId());
		}
		finally
		{
			tracker.close();
		}
	}

}
