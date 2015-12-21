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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.preferences.PreferenceInitializer;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.VersionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DatabaseConfigurer implements IRunnableWithProgress
{
	private ConnectionService connectionService;
	
	public DatabaseConfigurer()
	{
	}

	public void run(final IProgressMonitor monitor)
	{
		monitor.beginTask("Die Datenbank wird konfiguriert...", 2);
		try
		{
			if (this.connectionService != null)
			{
				VersionQuery query = (VersionQuery) connectionService.getQuery(Version.class);
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
				DomainQuery domainQuery = (DomainQuery) connectionService.getQuery(Domain.class);
				if (domainQuery.selectAll().isEmpty())
				{
					Domain domain = Domain.newInstance();
					domain.setDeleted(false);
					domain.setInserted(GregorianCalendar.getInstance());
					domainQuery.merge(domain);
				}
			}
		}
		finally
		{
			monitor.done();
		}
	}

	public IStatus configureDatabase(ConnectionService connectionService)
	{
		this.connectionService = connectionService;
		IStatus status = Status.OK_STATUS;
//		ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
//		try 
//		{
//			dialog.run(false, false, this);
//		} 
//		catch (InvocationTargetException e) 
//		{
//		} 
//		catch (InterruptedException e) 
//		{
//		}
		return status;
	}
}
