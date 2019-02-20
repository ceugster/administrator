package ch.eugster.events.utilities.console.database.manipulator;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.utilities.console.Activator;

public class CopyAllTeachersToNewAddressGroup implements CommandProvider
{
	public void _teachers(final CommandInterpreter commandInterpreter)
	{
		Map<String, String> arguments = new HashMap<String, String>();
		String argument = commandInterpreter.nextArgument();
		while (argument != null)
		{
			if (argument.contains("="))
			{
				String[] parts = argument.split("=");
				if (parts.length == 2)
				{
					arguments.put(parts[0], parts[1]);
				}
			}
			else
			{
				arguments.put(argument, argument);
			}
			argument = commandInterpreter.nextArgument();
		}

		if (arguments.get("-d") == null)
		{
			System.out.println("argument -d not provided!\n" + getHelp());
		}
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

			DomainQuery domainQuery = (DomainQuery) service.getQuery(Domain.class);
			Domain domain = domainQuery.selectByCode(arguments.get("-d"));
			if (domain == null)
			{
				System.out.println("Error: domain with code '" + arguments.get("-d") + "' does not exist.");
				return;
			}
			AddressGroupCategory category = null;
			AddressGroupCategoryQuery categoryQuery = (AddressGroupCategoryQuery)service.getQuery(AddressGroupCategory.class);
			List<AddressGroupCategory> categories = categoryQuery.selectByDomain(domain);
			for (AddressGroupCategory c : categories)
			{
				if (c.getName().equals("Alle Lehrer"))
				{
					category = c;
					break;
				}
			}
			if (category == null)
			{
				category = AddressGroupCategory.newInstance(domain);
				category.setName("Alle Lehrer");
			}
			else if (category.isDeleted())
			{
				category.setDeleted(false);
			}
			category = categoryQuery.merge(category);
			if (category.getId() == null)
			{
				System.out.println("Error: address group category '" + category.getName() + "' could not be inserted.");
				return;
			}
			AddressGroup addressGroup = AddressGroup.newInstance(category);
			addressGroup.setName(SimpleDateFormat.getDateTimeInstance().format(GregorianCalendar.getInstance().getTime()));
			AddressQuery addressQuery = (AddressQuery)service.getQuery(Address.class);
			Map<String, String> criteria = new HashMap<String, String>();
			criteria.put("name", "%Schul%");
			List<Address> addresses = addressQuery.selectAddresses(criteria);
			for (Address address : addresses)
			{
				if (!address.isDeleted())
				{
					List<LinkPersonAddress> links = address.getValidLinks();
					for (LinkPersonAddress link : links)
					{
						if (!link.isDeleted())
						{
							AddressGroupMember member = AddressGroupMember.newInstance(addressGroup, link);
							addressGroup.addAddressGroupMember(member);
						}
					}
				}
			}
			AddressGroupQuery addressGroupQuery = (AddressGroupQuery)service.getQuery(AddressGroup.class);
			addressGroup = addressGroupQuery.merge(addressGroup);
			if (addressGroup.getId() == null)
			{
				System.out.println("Error: address group '" + addressGroup.getName() + "' could not be inserted.");
				return;
			}
			System.out.println("OK.");
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
				.append("teachers - copy all teachers to a new address group\n")
				.append("   parameters:\n")
				.append("      -d=<Domaincode>\tUse domain with code <Domaincode>")
				.toString();
	}

}
