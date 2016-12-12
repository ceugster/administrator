package ch.eugster.events.persistence.database;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.User.UserStatus;
import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.queries.VersionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class PredefinedEntityChecker
{
	public PredefinedEntityChecker()
	{
	}

	/*
	 * Der Check soll nur durchgeführt werden, wenn der Server zum laufen
	 * gebracht wurde
	 */
	public IStatus check()
	{
		IStatus status = checkVersion();
		if (status.equals(Status.OK_STATUS))
			status = checkUser();

		return status;
	}

	private IStatus checkUser()
	{
		IStatus status = Status.OK_STATUS;
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			String username = System.getProperty("user.name");
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				UserQuery query = (UserQuery) service.getQuery(User.class);
				User user = query.selectByUsername(username);
				if (user == null)
				{
					user = User.newInstance();
					user.setUsername(username);
					user.setState(UserStatus.USER);
					user = query.merge(user);
					if (user != null)
					{
						User.setCurrent(user);
					}
				}
				else
				{
					User.setCurrent(user);
				}
			}
		}
		finally
		{
			tracker.close();
		}
		return status;
	}

	private IStatus checkVersion()
	{
		IStatus status = Status.OK_STATUS;
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				VersionQuery query = (VersionQuery) service.getQuery(Version.class);
				Version version = query.find(Version.class, Long.valueOf(1L));
				if (version == null)
				{
					version = Version.newInstance();
					version.setDataVersion(0);
					version.setStructureVersion(1);
					version = query.merge(version);
				}
			}
		}
		finally
		{
		tracker.close();
		}
		return status;
	}

}
