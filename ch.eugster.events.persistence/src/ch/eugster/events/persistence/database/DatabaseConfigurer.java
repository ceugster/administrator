/*
 * Created on 13.02.2009
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.persistence.database;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.preferences.PreferenceInitializer;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.VersionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DatabaseConfigurer
{
	public DatabaseConfigurer()
	{
	}

	private IStatus configure(final IProgressMonitor monitor)
	{
		IStatus status = Status.OK_STATUS;
		monitor.beginTask("Die Datenbank wird konfiguriert...", 1150);
		try
		{
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();

			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				VersionQuery query = (VersionQuery) service.getQuery(Version.class);
				Version version = query.find(Version.class, Long.valueOf(1L));
				if (version == null)
				{
					version = Version.newInstance();
					version.setDataVersion(Version.DATA_VERSION);
					version.setStructureVersion(Version.STRUCTURE_VERSION);
					version = query.merge(version);
					try
					{
						Preferences prefs = PreferenceInitializer.getServerNode();
						prefs.put(PreferenceInitializer.KEY_STARTUP, Integer.toString(1));
						prefs.flush();
					}
					catch (BackingStoreException e)
					{
						String msg = "Die Version in den Einstellungen konnte nicht aktualisiert werden.";
						status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, msg, e);
					}
				}
				else
				{
					if (version.getDataVersion() < Version.DATA_VERSION)
					{
						if (version.getDataVersion() == 1)
						{
						}
						version.setDataVersion(version.getDataVersion() + 1);
						version = query.merge(version);
					}
				}
				DomainQuery domainQuery = (DomainQuery) service.getQuery(Domain.class);
				if (domainQuery.selectAll().isEmpty())
				{
					Domain domain = Domain.newInstance();
					domain.setDeleted(false);
					domain.setInserted(GregorianCalendar.getInstance());
					domainQuery.merge(domain);
				}
			}
			tracker.close();
		}
		finally
		{
			monitor.done();
		}

		return status;
	}

	public IStatus configureDatabase()
	{
		IStatus status = Status.OK_STATUS;

		UIJob job = new UIJob("Configuring database...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				IStatus status = null;
				try
				{
					monitor.beginTask("Die Datenbank wird konfiguriert...", 100);
					status = DatabaseConfigurer.this.configure(new SubProgressMonitor(monitor, 100));
				}
				finally
				{
					monitor.done();
				}
				return status;
			}
		};
		job.schedule();

		return status;
	}

}
