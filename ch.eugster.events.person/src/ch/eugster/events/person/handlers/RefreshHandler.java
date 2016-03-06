package ch.eugster.events.person.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class RefreshHandler extends ConnectionServiceDependentAbstractHandler
{
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
		if (connectionService != null)
		{
			try
			{
				PersonQuery query = (PersonQuery) connectionService.getQuery(Person.class);
				return (Person) query.refresh(person);
			}
			catch (Exception e)
			{
				PersonQuery query = (PersonQuery) connectionService.getQuery(Person.class);
				return query.find(Person.class, person.getId());
			}
		}
		return null;
	}

	private Address refresh(Address address)
	{
		if (connectionService != null)
		{
			try
			{
				AddressQuery query = (AddressQuery) connectionService.getQuery(Address.class);
				return (Address) query.refresh(address);
			}
			catch (Exception e)
			{
				AddressQuery query = (AddressQuery) connectionService.getQuery(Address.class);
				return query.find(Address.class, address.getId());
			}
		}
		return null;
	}

	private LinkPersonAddress refresh(LinkPersonAddress link)
	{
		if (connectionService != null)
		{
			try
			{
				LinkPersonAddressQuery query = (LinkPersonAddressQuery) connectionService.getQuery(LinkPersonAddress.class);
				return (LinkPersonAddress) query.refresh(link);
			}
			catch (Exception e)
			{
				LinkPersonAddressQuery query = (LinkPersonAddressQuery) connectionService.getQuery(LinkPersonAddress.class);
				return query.find(LinkPersonAddress.class, link.getId());
			}
		}
		return null;
	}

}
