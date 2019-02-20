package ch.eugster.events.utilities.console.database.manipulator;

import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.utilities.console.Activator;

public class PersonWithoutChildrenRemover implements CommandProvider
{
	public void _clearlinks(final CommandInterpreter commandInterpreter)
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

			boolean merge = false;
			int deleted = 0;
			int count = 0;
			LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
			List<LinkPersonAddress> links = query.selectActiveLinks();
			for (LinkPersonAddress link : links)
			{
				if (!link.hasValidAddressGroupMembers())
				{
					if (!link.hasValidDonations())
					{
						if (!link.hasValidMembers())
						{
							if (!link.hasValidParticipants())
							{
								if (link.getGuide() == null || link.getGuide().isDeleted())
								{
									link.setDeleted(true);
									if (link.getPerson().getActiveLinks().size() == 1)
									{
										System.out.print("-");
										deleted++;
										merge = true;
									}
								}
							}
						}
					}
				}
				count++;
				if (merge)
				{
					link = query.merge(link);
					merge = false;
				}
				else
				{
					System.out.print(".");
				}
			}
			System.out.println("");
			System.out.println("Counted: " + count + "; deleted: " + deleted + ".");
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

}
