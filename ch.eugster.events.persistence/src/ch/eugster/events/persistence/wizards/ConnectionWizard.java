/*
 * Created on 13.02.2009
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.persistence.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.preferences.PreferenceInitializer;

public class ConnectionWizard extends Wizard
{
	private ConnectionWizardPage connectionPage;

	@Override
	public void addPages()
	{
		this.setDefaultPageImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("db.gif"));
		this.connectionPage = new ConnectionWizardPage("Datenbankverbindung");
		this.addPage(this.connectionPage);
	}

	@Override
	public boolean canFinish()
	{
		boolean canFinish = this.connectionPage.isPageComplete();
		return canFinish;
	}

	@Override
	public boolean performFinish()
	{
		Preferences serverPrefs = PreferenceInitializer.getServerNode();
		serverPrefs.put(PersistenceUnitProperties.JDBC_URL, this.connectionPage.getUrl());
		serverPrefs.put(PersistenceUnitProperties.JDBC_USER, this.connectionPage.getUser());
		serverPrefs.put(PersistenceUnitProperties.JDBC_PASSWORD, this.connectionPage.getPassword());
		serverPrefs.put(PersistenceUnitProperties.JDBC_DRIVER, this.connectionPage.getDriverName());

		try
		{
			serverPrefs.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}
		return true;
	}
}
