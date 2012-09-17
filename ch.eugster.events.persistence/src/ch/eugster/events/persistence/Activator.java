package ch.eugster.events.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;

import ch.eugster.events.persistence.database.DatabaseUpdater;
import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.preferences.PreferenceInitializer;
import ch.eugster.events.persistence.wizards.ConnectionWizard;

public class Activator extends AbstractUIPlugin
{
	public static final String PLUGIN_ID = "ch.eugster.events.persistence";

	public static final String PERSISTENCE_UNIT_NAME = "ch.eugster.events.persistence";

	private static Activator activator;

	public Activator()
	{
	}

	/**
	 * Prüft, ob die Verbindung gültig ist und falls ja, ob die Datenbank gültig
	 * ist.
	 * 
	 * @return IStatus
	 */
	private ResultType checkConnection()
	{
		ResultType result = null;
		do
		{
			try
			{
				result = this.createJdbcConnection();

			}
			catch (Exception e)
			{
				IStatus status = this.readProperties();
				if (status.equals(Status.CANCEL_STATUS))
				{
					result = ResultType.EXIT_PROGRAM;
				}
			}
		}
		while (result == null);
		return result;
	}

	private Version checkVersion(final java.sql.Connection con)
	{
		Version version = null;
		java.sql.Statement stm = null;
		java.sql.DatabaseMetaData dmd;
		java.sql.ResultSet rst = null;
		try
		{
			stm = con.createStatement();
			dmd = con.getMetaData();
			rst = dmd.getTables(null, null, "events_version", new String[] { "TABLE" });
			if (rst.next())
			{
				rst = stm.executeQuery("SELECT * FROM events_version");
				if (rst.next())
				{
					version = Version.newInstance();
					version.setDataVersion(rst.getInt("version_data"));
					version.setDeleted(rst.getBoolean("version_deleted"));
					version.setId(Long.valueOf(rst.getLong("version_id")));
					version.setStructureVersion(rst.getInt("version_structure"));
				}
				else
				{
					stm.execute("INSERT INTO events_version (version_id, version_data, version_structure, version_deleted, version_version) VALUES (1, 0, 0, false, 1)");
					rst = stm.executeQuery("SELECT * FROM events_version");
					if (rst.next())
					{
						version = Version.newInstance();
						version.setDataVersion(rst.getInt("version_data"));
						version.setDeleted(rst.getBoolean("version_deleted"));
						version.setId(Long.valueOf(rst.getLong("version_id")));
						version.setStructureVersion(rst.getInt("version_structure"));
					}
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (rst != null)
					rst.close();
				if (stm != null)
					stm.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return version;
	}

	private ResultType createJdbcConnection() throws ClassNotFoundException, SQLException
	{
		ResultType result = null;

		Map<String, Object> properties = getProperties();
		String driverName = (String) properties.get(PersistenceUnitProperties.JDBC_DRIVER);
		if (driverName.equals(com.mysql.jdbc.Driver.class.getName()))
		{
			Class.forName(driverName);
		}
		String url = (String) properties.get(PersistenceUnitProperties.JDBC_URL);
		String usr = (String) properties.get(PersistenceUnitProperties.JDBC_USER);
		String pwd = (String) properties.get(PersistenceUnitProperties.JDBC_PASSWORD);
		java.sql.Connection con = DriverManager.getConnection(url, usr, pwd);

		Version version = this.checkVersion(con);
		if (version == null)
		{
			/*
			 * Es wird davon ausgegangen, dass die Datenbank noch nicht
			 * initialisiert worden ist. Daher ist kein Update notwendig, die
			 * Datenbank wird beim Starten des EntityManagers automatisch
			 * initialisiert.
			 */
			result = ResultType.CONNECT_NORMAL;
		}
		else if (version.getStructureVersion() == Version.STRUCTURE_VERSION)
		{
			result = ResultType.CONNECT_NORMAL;
		}
		else
		{
			DatabaseUpdater updater = DatabaseUpdater.newInstance(driverName);
			result = updater.updateStructure(con);
		}

		if (result.getStatus() != null)
		{
			ErrorDialog dialog = new ErrorDialog(new Shell(Display.getDefault()), "Verbindungsproblem",
					result.getMessage(), result.getStatus(), 0);
			dialog.open();
		}
		else if (result.getMessage() != null)
		{
			MessageDialog dialog = new MessageDialog(new Shell(Display.getDefault()), "Verbindungsproblem", null,
					result.getMessage(), MessageDialog.INFORMATION, new String[] { "OK" }, 0);
			dialog.open();
		}
		return result;
	}

	public Map<String, Object> getProperties()
	{
		Preferences prefs = PreferenceInitializer.getServerNode();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER,
				prefs.get(PersistenceUnitProperties.JDBC_DRIVER, com.mysql.jdbc.Driver.class.getName()));
		properties.put(PersistenceUnitProperties.JDBC_URL,
				prefs.get(PersistenceUnitProperties.JDBC_URL, "jdbc:mysql:events"));
		properties.put(PersistenceUnitProperties.JDBC_USER, prefs.get(PersistenceUnitProperties.JDBC_USER, "events"));
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD,
				prefs.get(PersistenceUnitProperties.JDBC_PASSWORD, "events"));

		properties.put(PersistenceUnitProperties.CLASSLOADER, this.getClass().getClassLoader());

		properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_ONLY);
		properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE, PersistenceUnitProperties.DDL_BOTH_GENERATION);
		properties.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE, "create_tables.sql");
		properties.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE, "drop_tables.sql");

		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, Level.FINEST.getName());

		// properties.put(PersistenceUnitProperties.SESSION_CUSTOMIZER,
		// "ch.eugster.events.persistence.database.DatabaseDataUpdater");
		return properties;
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put("WIZARD", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/defcon_wiz.png")));
	}

	private IStatus readProperties()
	{
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		ConnectionWizard wizard = new ConnectionWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.setBlockOnOpen(true);
		return (dialog.open() == 0) ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	@Override
	public void start(final BundleContext context) throws Exception
	{
		super.start(context);
		Activator.activator = this;

		ResultType result = checkConnection();
		if (result.equals(ResultType.EXIT_PROGRAM))
		{
			if (PlatformUI.isWorkbenchRunning())
			{
				PlatformUI.getWorkbench().close();
			}
			else
			{
				System.exit(-1);
			}
		}

	}

	@Override
	public void stop(final BundleContext context) throws Exception
	{
		Activator.activator = null;
		super.stop(context);
	}

	public static Activator getDefault()
	{
		return Activator.activator;
	}

	public enum ResultType
	{
		CONNECT_NORMAL, UPDATE_CURRENT_VERSION, NO_CONNECTION, EXIT_PROGRAM;

		private String message;

		private IStatus status;

		public String getMessage()
		{
			return this.message;
		}

		public IStatus getStatus()
		{
			return this.status;
		}

		public void setMessage(final String message)
		{
			this.message = message;
		}

		public void setStatus(final IStatus status)
		{
			this.status = status;
		}
	}
}
