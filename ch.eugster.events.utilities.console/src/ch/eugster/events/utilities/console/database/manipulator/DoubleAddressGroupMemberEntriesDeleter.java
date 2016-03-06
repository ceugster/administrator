package ch.eugster.events.utilities.console.database.manipulator;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.utilities.console.Activator;

public class DoubleAddressGroupMemberEntriesDeleter implements CommandProvider
{
	private int counter = 0;

	private int doubleCounter = 0;

	public void _deletedoubleaddressgroupmemberentries(final CommandInterpreter commandInterpreter)
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getContext(), ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service == null)
			{
				System.out.println("Database connection not established.");
				return;
			}

			counter = 0;
			doubleCounter = 0;
			DomainQuery domainQuery = (DomainQuery) service.getQuery(Domain.class);
			List<Domain> domains = domainQuery.selectAll();
			for (Domain domain : domains)
			{
				compute(service, domain);
			}
			System.out.println(counter + " " + doubleCounter);
		}
		finally
		{
			tracker.close();
		}
	}

	@Override
	public String getHelp()
	{
		StringBuilder help = new StringBuilder();
		return help
				.append("\tdeletedoubleaddressgroupmemberentries - deletes double address group member entries from database\n")
				.toString();
	}

	private void compute(ConnectionService service, Domain domain)
	{
		AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) service.getQuery(AddressGroupCategory.class);
		List<AddressGroupCategory> categories = query.selectByDomain(domain);
		for (AddressGroupCategory category : categories)
		{
			compute(service, category);
		}
	}

	private void compute(ConnectionService service, AddressGroupCategory category)
	{
		Calendar calendar = GregorianCalendar.getInstance();
		List<AddressGroup> addressGroups = category.getAddressGroups();
		for (AddressGroup addressGroup : addressGroups)
		{
			boolean deleted = false;
			Map<String, AddressGroupMember> members = new HashMap<String, AddressGroupMember>();
			List<AddressGroupMember> addressGroupMembers = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember addressGroupMember : addressGroupMembers)
			{
				String id = (addressGroupMember.getLink() == null || addressGroupMember.getLink().isDeleted() || addressGroupMember
						.getLink().getPerson().isDeleted()) ? "A" + addressGroupMember.getAddress().getId() : "P"
						+ addressGroupMember.getLink().getId();
				AddressGroupMember member = members.get(id);
				if (member == null)
				{
					members.put(id, addressGroupMember);
					counter++;
				}
				else
				{
					doubleCounter++;
					addressGroupMember.setUpdated(calendar);
					addressGroupMember.setDeleted(true);
					deleted = true;
				}
			}
			if (deleted)
			{
				AddressGroupQuery query = (AddressGroupQuery) service.getQuery(AddressGroup.class);
				addressGroup = query.merge(addressGroup);
			}
			members.clear();
		}
	}

}
