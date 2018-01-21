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
import org.osgi.service.log.LogService;
import org.osgi.service.prefs.Preferences;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.database.DatabaseUpdater;
import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.preferences.PreferenceInitializer;
import ch.eugster.events.persistence.wizards.ConnectionWizard;

public class Activator extends AbstractUIPlugin
{
	public static final String PLUGIN_ID = "ch.eugster.events.persistence";

	public static final String PERSISTENCE_UNIT_NAME = "ch.eugster.events.persistence";

	private static LogService log;

	private static Activator activator;

	private ServiceTracker<LogService, LogService> logTracker;

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
				log(LogService.LOG_INFO, "Start creating JDBC connection");
				result = this.createJdbcConnection();
				log(LogService.LOG_INFO, "End creating JDBC connection.");
			}
			catch (Exception e)
			{
				log(LogService.LOG_INFO,
						"JDBC connection could not be created. Reading connection properties from user");
				IStatus status = this.readProperties();
				log(LogService.LOG_INFO, "JDBC connection properties provided.");
				if (status.equals(Status.CANCEL_STATUS))
				{
					log(LogService.LOG_INFO, "User canceled providing JDBC connection properties.");
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

		log(LogService.LOG_INFO, "Read connection properties from file");
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

		log(LogService.LOG_INFO, "Check data version");
		Version version = this.checkVersion(con);
		if (version == null)
		{
			log(LogService.LOG_INFO, "No version table found");
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
			log(LogService.LOG_INFO, "Database structure version is actual (" + Version.STRUCTURE_VERSION + ".");
			result = ResultType.CONNECT_NORMAL;
		}
		else
		{
			log(LogService.LOG_INFO, "Database structure version is: " + version.getStructureVersion()
					+ "; current version should be: " + Version.STRUCTURE_VERSION + ". Updating database structure.");
			DatabaseUpdater updater = DatabaseUpdater.newInstance(driverName);
			result = updater.updateStructure(con);
			log(LogService.LOG_INFO, "Database updated to structure version " + Version.STRUCTURE_VERSION);
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
		/*
		 * properties.put(PersistenceUnitProperties.DDL_GENERATION,
		 * PersistenceUnitProperties.CREATE_ONLY);
		 * properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE,
		 * PersistenceUnitProperties.DDL_BOTH_GENERATION);
		 * properties.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE,
		 * "create_tables.sql");
		 * properties.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE,
		 * "drop_tables.sql");
		 */
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, Level.WARNING.getName());

		// properties.put(PersistenceUnitProperties.SESSION_CUSTOMIZER,
		// "ch.eugster.events.persistence.database.DatabaseDataUpdater");
		return properties;
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put("WIZARD", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/defcon_wiz.png")));
		imageRegistry.put("phone", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/phone_16.png")));
		imageRegistry.put("mobile", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/mobile_16.png")));
		imageRegistry.put("email", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/email_16.png")));
		imageRegistry.put("browse", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/browse_16.png")));
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

		logTracker = new ServiceTracker<LogService, LogService>(context, LogService.class, null);
		logTracker.open();
		log = (LogService) logTracker.getService();

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
		logTracker.close();
		Activator.activator = null;
		super.stop(context);
	}

	public static Activator getDefault()
	{
		return Activator.activator;
	}

	public static void log(final int level, final String message)
	{
		if (log instanceof LogService)
		{
			log.log(level, message);
		}
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
