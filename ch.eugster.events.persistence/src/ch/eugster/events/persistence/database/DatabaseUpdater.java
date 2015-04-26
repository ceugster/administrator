package ch.eugster.events.persistence.database;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.log.LogService;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.Activator.ResultType;
import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.wizards.ConnectionWizard;

public abstract class DatabaseUpdater
{
	private String collectSelectFields(final java.sql.Connection con)
	{
		StringBuilder builder = new StringBuilder("SELECT ");
		String[] fields = new String[] { "global_settings_person_id_format", "global_settings_person_label_format",
				"global_settings_address_label_format", "global_settings_person_has_domain",
				"global_settings_person_domain_mandatory" };

		List<String> fs = new ArrayList<String>();
		for (String field : fields)
		{
			if (this.columnExists(con, "events_global_settings", field))
			{
				fs.add(field);
			}
		}
		String[] fsas = fs.toArray(new String[0]);
		if (fsas.length == 0)
		{
			return null;
		}
		for (int i = 0; i < fsas.length; i++)
		{
			builder.append(fsas[i]);
			if (i < fsas.length - 1)
			{
				builder.append(", ");
			}
		}
		return builder.append(" FROM events_global_settings;").toString();
	}

	private String collectUpdateFields(final ResultSet resultSet)
	{
		StringBuilder builder = new StringBuilder("UPDATE events_person_settings SET ");
		String[] fields = new String[] { "global_settings_person_id_format", "global_settings_person_label_format",
				"global_settings_address_label_format", "global_settings_person_has_domain",
				"global_settings_person_domain_mandatory" };

		Map<String, Object> objects = new HashMap<String, Object>();
		for (String field : fields)
		{
			try
			{
				Object value = resultSet.getObject(field);
				objects.put(field, value);
			}
			catch (SQLException e)
			{
				// Do nothing
			}
		}
		int i = 0;
		Iterator<Entry<String, Object>> entries = objects.entrySet().iterator();
		while (entries.hasNext())
		{
			Entry<String, Object> entry = entries.next();
			if (entry.getValue() instanceof String)
			{
				builder.append(entry.getKey()).append(" = '" + entry.getValue() + "'");
			}
			else if (entry.getValue() instanceof Integer)
			{
				builder.append(entry.getKey()).append(" = " + entry.getValue());
			}
			if (i < objects.size() - 1)
			{
				builder.append(", ");
			}
			i++;
		}
		return builder.append(";").toString();
	}

	protected boolean columnExists(final java.sql.Connection con, final String tableName, final String columnName)
	{
		try
		{
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getColumns(null, null, tableName, columnName);

			if (!rs.first())
			{
				return false;
			}
			while (!rs.isAfterLast())
			{
				if (rs.getString(rs.findColumn("COLUMN_NAME")).equals(columnName))
				{
					return true;
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}

	protected boolean executeSqlQuery(final java.sql.Connection connection, final String query) throws SQLException
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			try
			{
				return statement.execute(query);
			}
			finally
			{
				statement.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	protected ResultSet executeSqlQueryWithResultSet(final java.sql.Connection connection, final String query)
			throws SQLException
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeQuery(query);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	protected abstract String getClobTypeName();

	protected abstract String getCurrentDate();

	protected abstract String getEngine();

	protected abstract String getCharset();

	private void log(final int level, final String message)
	{
		Activator.log(level, message);
	}

	protected IStatus readProperties()
	{
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		ConnectionWizard wizard = new ConnectionWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.setBlockOnOpen(true);
		return (dialog.open() == 0) ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	protected boolean rowExists(final java.sql.Connection con, final String table, final String column,
			final String value, final String del)
	{
		try
		{
			PreparedStatement stm = con.prepareStatement("SELECT * FROM " + table + " WHERE " + column + " = " + del
					+ value + del);
			ResultSet rs = stm.executeQuery();
			if (rs.first())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}

	protected boolean tableExists(final java.sql.Connection con, final String tableName)
	{
		try
		{
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getTables(null, null, tableName, new String[] { "TABLE" });

			if (!rs.first())
			{
				return false;
			}
			while (!rs.isAfterLast())
			{
				if (rs.getString(rs.findColumn("TABLE_NAME")).equals(tableName))
				{
					return true;
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}

	public ResultType updateStructure(final java.sql.Connection con)
	{
		ResultType result = ResultType.CONNECT_NORMAL;
		Statement stm = null;
		ResultSet rst = null;
		try
		{
			log(LogService.LOG_INFO, "Connecting to database.");
			stm = con.createStatement();
			log(LogService.LOG_INFO, "Obtaining version record.");
			rst = stm.executeQuery("SELECT * FROM events_version");
			rst.next();
			int structureVersion = rst.getInt("version_structure");
			rst.close();
			if (structureVersion == Version.STRUCTURE_VERSION)
			{
				result = ResultType.CONNECT_NORMAL;
			}
			else if (structureVersion < Version.STRUCTURE_VERSION)
			{
				result = ResultType.UPDATE_CURRENT_VERSION;
				while (structureVersion < Version.STRUCTURE_VERSION)
				{
					boolean ok = true;
					if (structureVersion == 2)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_global_settings", "global_settings_simple_person"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings ADD COLUMN global_settings_simple_person SMALLINT DEFAULT 0;");
						}
					}
					if (structureVersion == 3)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_address_group_member", "address_group_member_copied_from"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_address_group_member ADD COLUMN address_group_member_copied_from BIGINT;");
						}
					}
					if (structureVersion == 4)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!tableExists(con, "events_visit_settings"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_visit_settings (")
											.append("visit_settings_id BIGINT UNSIGNED NOT NULL, ")
											.append("visit_settings_deleted SMALLINT DEFAULT NULL, ")
											.append("visit_settings_version INTEGER UNSIGNED DEFAULT NULL, ")
											.append("visit_settings_inserted DATETIME DEFAULT NULL, ")
											.append("visit_settings_updated DATETIME DEFAULT NULL, ")
											.append("visit_settings_user_id BIGINT UNSIGNED DEFAULT NULL, ")
											.append("visit_settings_default_address_type_id BIGINT UNSIGNED DEFAULT NULL, ")
											.append("visit_settings_start_range DATETIME DEFAULT NULL, ")
											.append("visit_settings_end_range DATETIME DEFAULT NULL)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_visit_settings ADD PRIMARY KEY (visit_settings_id)");
							ok = executeSqlQuery(con,
									"INSERT INTO events_visit_settings (visit_settings_id, visit_settings_deleted, visit_settings_version) VALUES(1, 0, 0)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_visit_settings_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_visit_settings_id_seq', 2);");
							}
						}

						if (!tableExists(con, "events_appliance"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_appliance (")
											.append("appliance_id BIGINT UNSIGNED NOT NULL, ")
											.append("appliance_deleted TINYINT DEFAULT NULL, ")
											.append("appliance_version INTEGER UNSIGNED DEFAULT NULL, ")
											.append("appliance_inserted DATETIME DEFAULT NULL, ")
											.append("appliance_updated DATETIME DEFAULT NULL, ")
											.append("appliance_user_id BIGINT UNSIGNED DEFAULT NULL, ")
											.append("appliance_name VARCHAR(255) DEFAULT NULL, ")
											.append("appliance_description " + this.getClobTypeName() + ")").toString());
							ok = executeSqlQuery(con, "ALTER TABLE events_appliance ADD PRIMARY KEY (appliance_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_appliance_user_id ON events_appliance(appliance_user_id)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_appliance_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_appliance_id_seq', 0);");
							}
						}

						if (!tableExists(con, "events_visit_appliance"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_visit_appliance (")
											.append("visit_appliance_id BIGINT UNSIGNED NOT NULL, ")
											.append("visit_appliance_deleted TINYINT DEFAULT NULL, ")
											.append("visit_appliance_version INTEGER DEFAULT NULL, ")
											.append("visit_appliance_inserted DATETIME DEFAULT NULL, ")
											.append("visit_appliance_updated DATETIME DEFAULT NULL, ")
											.append("visit_appliance_user_id BIGINT DEFAULT NULL, ")
											.append("visit_appliance_visit_id BIGINT DEFAULT NULL, ")
											.append("visit_appliance_appliance_id BIGINT DEFAULT NULL)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_visit_appliance ADD PRIMARY KEY (visit_appliance_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_appliance_appliance_id ON events_visit_appliance(visit_appliance_appliance_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_appliance_visit_id ON events_visit_appliance(visit_appliance_visit_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_appliance_user_id ON events_visit_appliance(visit_appliance_user_id)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_visit_appliance_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_visit_appliance_id_seq', 0);");
							}
						}

						if (!tableExists(con, "events_school_class"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_school_class (")
											.append("school_class_id BIGINT UNSIGNED NOT NULL, ")
											.append("school_class_deleted TINYINT DEFAULT NULL, ")
											.append("school_class_version INTEGER DEFAULT NULL, ")
											.append("school_class_inserted DATETIME DEFAULT NULL, ")
											.append("school_class_updated DATETIME DEFAULT NULL, ")
											.append("school_class_user_id BIGINT DEFAULT NULL, ")
											.append("school_class_name VARCHAR(255) DEFAULT NULL, ")
											.append("school_class_level INTEGER DEFAULT NULL)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_school_class ADD PRIMARY KEY (school_class_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_school_class_name ON events_school_class(school_class_name)");

							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (1, \"Kindergarten\", 0, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (2, \"1. Klasse Primarschule\", 1, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (3, \"2. Klasse Primarschule\", 1, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (4, \"3. Klasse Primarschule\", 1, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (5, \"4. Klasse Primarschule\", 2, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (6, \"5. Klasse Primarschule\", 2, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (7, \"6. Klasse Primarschule\", 2, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (8, \"7. Klasse Primarschule\", 3, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (9, \"8. Klasse Primarschule\", 3, 0, 0);");
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_school_class (school_class_id, school_class_name, school_class_level, school_class_version, school_class_deleted) VALUES (10, \"1. Klasse Sekundarschule\", 3, 0, 0);");
							if (!rowExists(con, "events_sequence", "seq_name", "events_school_class_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_school_class_id_seq', 11);");
							}
						}

						if (!tableExists(con, "events_teacher"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_teacher (")
											.append("teacher_id BIGINT UNSIGNED NOT NULL, ")
											.append("teacher_deleted TINYINT DEFAULT NULL, ")
											.append("teacher_version INTEGER DEFAULT NULL, ")
											.append("teacher_inserted DATETIME DEFAULT NULL, ")
											.append("teacher_updated DATETIME DEFAULT NULL, ")
											.append("teacher_user_id BIGINT DEFAULT NULL, ")
											.append("teacher_pa_link_id BIGINT DEFAULT NULL, ")
											.append("teacher_selected_phone INTEGER DEFAULT NULL, ")
											.append("teacher_best_reach_time " + this.getClobTypeName()
													+ " DEFAULT NULL, ")
											.append("teacher_selected_email INTEGER DEFAULT NULL)").toString());
							ok = executeSqlQuery(con, "ALTER TABLE events_teacher ADD PRIMARY KEY (teacher_id)");
							ok = executeSqlQuery(con, "CREATE INDEX idx_teacher_id ON events_teacher(teacher_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_teacher_user_id ON events_teacher(teacher_user_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_teacher_pa_link_id ON events_teacher(teacher_pa_link_id)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_teacher_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_teacher_id_seq', 0);");
							}
						}

						if (!tableExists(con, "events_visit_theme"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_visit_theme (")
											.append("visit_theme_id BIGINT UNSIGNED NOT NULL, ")
											.append("visit_theme_deleted TINYINT DEFAULT NULL, ")
											.append("visit_theme_version INTEGER DEFAULT NULL, ")
											.append("visit_theme_inserted DATETIME DEFAULT NULL, ")
											.append("visit_theme_updated DATETIME DEFAULT NULL, ")
											.append("visit_theme_user_id BIGINT DEFAULT NULL, ")
											.append("visit_theme_color INTEGER DEFAULT NULL, ")
											.append("visit_theme_description " + this.getClobTypeName()
													+ " DEFAULT NULL, ")
											.append("visit_theme_name VARCHAR(255) DEFAULT NULL)").toString());
							ok = executeSqlQuery(con, "ALTER TABLE events_visit_theme ADD PRIMARY KEY (visit_theme_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_theme_id ON events_visit_theme(visit_theme_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_theme_user_id ON events_visit_theme(visit_theme_user_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_theme_name ON events_visit_theme(visit_theme_name)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_visit_theme_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_visit_theme_id_seq', 0);");
							}
						}

						if (!tableExists(con, "events_visitor"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_visitor (")
											.append("visitor_id BIGINT UNSIGNED NOT NULL, ")
											.append("visitor_deleted TINYINT DEFAULT NULL, ")
											.append("visitor_version INTEGER DEFAULT NULL, ")
											.append("visitor_pa_link_id BIGINT DEFAULT NULL, ")
											.append("visitor_updated DATETIME DEFAULT NULL, ")
											.append("visitor_user_id BIGINT DEFAULT NULL, ")
											.append("visitor_selected_phone INTEGER DEFAULT NULL, ")
											.append("visitor_selected_email INTEGER DEFAULT NULL, ")
											.append("visitor_inserted DATETIME DEFAULT NULL)").toString());
							ok = executeSqlQuery(con, "ALTER TABLE events_visitor ADD PRIMARY KEY (visitor_id)");
							ok = executeSqlQuery(con, "CREATE INDEX idx_visitor_id ON events_visitor(visitor_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visitor_user_id ON events_visitor(visitor_user_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visitor_pa_link_id ON events_visitor(visitor_pa_link_id)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_visitor_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_visitor_id_seq', 0);");
							}
						}

						if (!tableExists(con, "events_visit"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_visit (")
											.append("visit_id BIGINT UNSIGNED NOT NULL, ")
											.append("visit_user_id BIGINT DEFAULT NULL, ")
											.append("visit_updated DATETIME DEFAULT NULL, ")
											.append("visit_inserted DATETIME DEFAULT NULL, ")
											.append("visit_class_name VARCHAR(255) DEFAULT NULL, ")
											.append("visit_best_reach_time VARCHAR(255) DEFAULT NULL, ")
											.append("visit_selected_phone INTEGER DEFAULT NULL, ")
											.append("visit_floor VARCHAR(255) DEFAULT NULL, ")
											.append("visit_class_room VARCHAR(255) DEFAULT NULL, ")
											.append("visit_teacher_id BIGINT DEFAULT NULL, ")
											.append("visit_version INTEGER DEFAULT NULL, ")
											.append("visit_deleted SMALLINT DEFAULT 0, ")
											.append("visit_school_class_id BIGINT DEFAULT NULL, ")
											.append("visit_visit_theme_id BIGINT DEFAULT NULL, ")
											.append("visit_start DATETIME DEFAULT NULL, ")
											.append("visit_end DATETIME DEFAULT NULL, ")
											.append("visit_selected_email INTEGER DEFAULT NULL, ")
											.append("visit_pupils INTEGER DEFAULT NULL, ")
											.append("visit_scool_class_id BIGINT DEFAULT NULL, ")
											.append("visit_state INTEGER DEFAULT NULL)").toString());
							ok = executeSqlQuery(con, "ALTER TABLE events_visit ADD PRIMARY KEY (visit_id)");
							ok = executeSqlQuery(con, "CREATE INDEX idx_visit_id ON events_visit(visit_id)");
							ok = executeSqlQuery(con, "CREATE INDEX idx_visit_user_id ON events_visit(visit_user_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_teacher_id ON events_visit(visit_teacher_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_visit_theme_id ON events_visit(visit_visit_theme_id)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_visit_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_visit_id_seq', 0);");
							}
						}

						if (!tableExists(con, "events_visit_visitor"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_visit_visitor (")
											.append("visit_visitor_id BIGINT UNSIGNED NOT NULL, ")
											.append("visit_visitor_user_id BIGINT DEFAULT NULL, ")
											.append("visit_visitor_updated DATETIME DEFAULT NULL, ")
											.append("visit_visitor_inserted DATETIME DEFAULT NULL, ")
											.append("visit_visitor_version INTEGER DEFAULT NULL, ")
											.append("visit_visitor_deleted SMALLINT DEFAULT 0, ")
											.append("visit_visitor_visit_id BIGINT DEFAULT NULL, ")
											.append("visit_visitor_visitor_id BIGINT DEFAULT NULL, ")
											.append("visit_visitor_type INTEGER DEFAULT NULL, ")
											.append("visit_visitor_selected_email INTEGER DEFAULT NULL, ")
											.append("visit_visitor_selected_phone INTEGER DEFAULT NULL)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_visit_visitor ADD PRIMARY KEY (visit_visitor_id)");
							ok = executeSqlQuery(con, "CREATE INDEX visit_visitor_id ON events_visit(visit_visitor_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_visitor_user_id ON events_visit(visit_visitor_user_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_visitor_visit_id ON events_visit(visit_visitor_visit_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_visit_visitor_visitor_id ON events_visit(visit_visitor_visitor_id)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_visit_visitor_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_visit_visitor_id_seq', 0);");
							}
						}
					}
					if (structureVersion == 5)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!tableExists(con, "events_email_account"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_email_account (")
											.append("email_account_id BIGINT UNSIGNED NOT NULL, ")
											.append("email_account_user_id BIGINT DEFAULT NULL, ")
											.append("email_account_updated DATETIME DEFAULT NULL, ")
											.append("email_account_inserted DATETIME DEFAULT NULL, ")
											.append("email_account_version INTEGER DEFAULT NULL, ")
											.append("email_account_deleted SMALLINT DEFAULT 0, ")
											.append("email_account_type INTEGER DEFAULT 0, ")
											.append("email_account_host VARCHAR(255) DEFAULT NULL, ")
											.append("email_account_port INTEGER DEFAULT NULL, ")
											.append("email_account_auth SMALLINT DEFAULT 0, ")
											.append("email_account_ssl_enable SMALLINT DEFAULT 0, ")
											.append("email_account_starttls_enable SMALLINT DEFAULT 0, ")
											.append("email_account_username VARCHAR(255) DEFAULT NULL, ")
											.append("email_account_password VARCHAR(255) DEFAULT NULL)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_email_account ADD PRIMARY KEY (email_account_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_email_account_id ON events_email_account(email_account_id)");
							if (!rowExists(con, "events_sequence", "seq_name", "events_email_account_id_seq", "'"))
							{
								ok = executeSqlQuery(con,
										"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_email_account_id_seq', 0);");
							}
						}
					}
					if (structureVersion == 6)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!tableExists(con, "events_person_settings"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_person_settings (")
											.append("person_settings_id BIGINT UNSIGNED NOT NULL, ")
											.append("person_settings_user_id BIGINT DEFAULT NULL, ")
											.append("person_settings_updated DATETIME DEFAULT NULL, ")
											.append("person_settings_inserted DATETIME DEFAULT NULL, ")
											.append("person_settings_version INTEGER DEFAULT NULL, ")
											.append("person_settings_deleted SMALLINT DEFAULT 0, ")
											.append("person_settings_editor_selector INTEGER DEFAULT 0, ")
											.append("person_settings_id_format VARCHAR(255), ")
											.append("person_settings_person_label_format VARCHAR(255), ")
											.append("person_settings_address_label_format VARCHAR(255), ")
											.append("person_settings_person_has_domain SMALLINT DEFAULT 0, ")
											.append("person_settings_person_domain_mandatory SMALLINT DEFAULT 0)")
											.toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_person_settings ADD PRIMARY KEY (person_settings_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_person_settings_id ON events_person_settings(person_settings_id)");
						}
						if (!rowExists(con, "events_sequence", "seq_name", "events_person_settings_id_seq", "'"))
						{
							ok = executeSqlQuery(con,
									"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_person_settings_id_seq', 1);");
						}
						if (!rowExists(con, "events_person_settings", "person_settings_id", "1", ""))
						{
							ok = executeSqlQuery(
									con,
									"INSERT INTO events_person_settings (person_settings_id, person_settings_version, person_settings_deleted, person_settings_editor_selector) VALUES (1, 0, 0, 0);");
							Statement statement = con.createStatement();
							String sql = collectSelectFields(con);
							if (sql != null)
							{
								ResultSet resultSet = statement.executeQuery(sql);
								if (resultSet.next())
								{
									sql = collectUpdateFields(resultSet);
									ok = executeSqlQuery(con, sql);
								}
								resultSet.close();
							}
							statement.close();
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_id_format"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_id_format");
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_label_format"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_label_format");
						}
						if (columnExists(con, "events_global_settings", "global_settings_address_label_format"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_address_label_format");
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_has_domain"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_has_domain");
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_domain_mandatory"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_domain_mandatory");
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_form_personal_male"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_form_personal_male");
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_form_personal_female"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_form_personal_female");
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_title_male"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_title_male");
						}
						if (columnExists(con, "events_global_settings", "global_settings_person_title_female"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_person_title_female");
						}
						if (columnExists(con, "events_global_settings", "global_settings_simple_person"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_global_settings DROP COLUMN global_settings_simple_person");
						}
						if (!tableExists(con, "events_field_extension"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_field_extension (")
											.append("field_extension_id BIGINT UNSIGNED NOT NULL, ")
											.append("field_extension_user_id BIGINT DEFAULT NULL, ")
											.append("field_extension_updated DATETIME DEFAULT NULL, ")
											.append("field_extension_inserted DATETIME DEFAULT NULL, ")
											.append("field_extension_version INTEGER DEFAULT NULL, ")
											.append("field_extension_deleted SMALLINT DEFAULT 0, ")
											.append("field_extension_type INTEGER DEFAULT 0, ")
											.append("field_extension_target INTEGER DEFAULT NULL, ")
											.append("field_extension_label VARCHAR(255) DEFAULT NULL, ")
											.append("field_extension_default_value " + this.getClobTypeName() + ", ")
											.append("field_extension_max_val INTEGER DEFAULT 0, ")
											.append("field_extension_min_val INTEGER DEFAULT 0, ")
											.append("field_extension_width_hint INTEGER DEFAULT 0, ")
											.append("field_extension_height_hint INTEGER DEFAULT 0, ")
											.append("field_extension_style INTEGER DEFAULT 0, ")
											.append("field_extension_decimal INTEGER DEFAULT 0)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_field_extension ADD PRIMARY KEY (field_extension_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_field_extension_id ON events_field_extension(field_extension_id)");
						}
						if (!rowExists(con, "events_sequence", "seq_name", "events_field_extension_id_seq", "'"))
						{
							ok = executeSqlQuery(con,
									"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_field_extension_id_seq', 0);");
						}
						if (!tableExists(con, "events_extended_field"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_extended_field (")
											.append("extended_field_id BIGINT UNSIGNED NOT NULL, ")
											.append("extended_field_user_id BIGINT DEFAULT NULL, ")
											.append("extended_field_updated DATETIME DEFAULT NULL, ")
											.append("extended_field_inserted DATETIME DEFAULT NULL, ")
											.append("extended_field_version INTEGER DEFAULT NULL, ")
											.append("extended_field_deleted SMALLINT DEFAULT 0, ")
											.append("extended_field_discriminator VARCHAR(5) DEFAULT NULL, ")
											.append("extended_field_owner_id BIGINT DEFAULT NULL, ")
											.append("extended_field_field_extension_id INTEGER DEFAULT 0, ")
											.append("extended_field_value " + this.getClobTypeName() + " DEFAULT NULL)")
											.toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_extended_field ADD PRIMARY KEY (extended_field_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_extended_field_id ON events_extended_field(extended_field_id)");
						}
						if (!rowExists(con, "events_sequence", "seq_name", "events_extended_field_id_seq", "'"))
						{
							ok = executeSqlQuery(con,
									"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_extended_field_id_seq', 0);");
						}
					}
					if (structureVersion == 7)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!tableExists(con, "events_user_property"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_user_property (")
											.append("user_property_id BIGINT UNSIGNED NOT NULL, ")
											.append("user_property_user_id BIGINT DEFAULT NULL, ")
											.append("user_property_updated DATETIME DEFAULT NULL, ")
											.append("user_property_inserted DATETIME DEFAULT NULL, ")
											.append("user_property_version INTEGER DEFAULT NULL, ")
											.append("user_property_deleted SMALLINT DEFAULT 0, ")
											.append("user_property_key VARCHAR(255) DEFAULT null, ")
											.append("user_property_value " + this.getClobTypeName() + " DEFAULT NULL, ")
											.append("user_property_parent_id BIGINT)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_user_property ADD PRIMARY KEY (user_property_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_user_property_id ON events_user_property(user_property_id)");
						}
						if (!rowExists(con, "events_sequence", "seq_name", "events_user_property_id_seq", "'"))
						{
							ok = executeSqlQuery(con,
									"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_user_property_id_seq', 1);");
						}
					}
					if (structureVersion == 8)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!tableExists(con, "events_address_group_link"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("CREATE TABLE events_address_group_link (")
											.append("address_group_link_id BIGINT UNSIGNED NOT NULL, ")
											.append("address_group_link_user_id BIGINT DEFAULT NULL, ")
											.append("address_group_link_updated DATETIME DEFAULT NULL, ")
											.append("address_group_link_inserted DATETIME DEFAULT NULL, ")
											.append("address_group_link_version INTEGER DEFAULT NULL, ")
											.append("address_group_link_deleted SMALLINT DEFAULT 0, ")
											.append("address_group_link_parent_id BIGINT DEFAULT NULL, ")
											.append("address_group_link_child_id BIGINT DEFAULT NULL)").toString());
							ok = executeSqlQuery(con,
									"ALTER TABLE events_address_group_link ADD PRIMARY KEY (address_group_link_id)");
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_address_group_link_id ON events_address_group_link(address_group_link_id)");
						}
						if (!rowExists(con, "events_sequence", "seq_name", "events_address_group_link_id_seq", "'"))
						{
							ok = executeSqlQuery(con,
									"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_address_group_link_id_seq', 1);");
						}
					}
					if (structureVersion == 9)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_field_extension", "field_extension_searchable"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_field_extension ").append(
											"ADD COLUMN field_extension_searchable SMALLINT DEFAULT 0").toString());
							ok = executeSqlQuery(con,
									"CREATE INDEX idx_field_extension_searchable ON events_field_extension(field_extension_searchable)");
						}
					}
					if (structureVersion == 10)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_person_settings", "person_settings_editor_section_behaviour"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_person_settings ").append(
											"ADD COLUMN person_settings_editor_section_behaviour SMALLINT DEFAULT 0")
											.toString());
						}
					}
					if (structureVersion == 11)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (columnExists(con, "events_pa_link", "pa_link_mailing_address"))
						{
							ok = executeSqlQuery(con, "ALTER TABLE events_pa_link DROP COLUMN pa_link_mailing_address");
						}
						if (columnExists(con, "events_pa_link", "pa_link_manual_mailing_address"))
						{
							ok = executeSqlQuery(con,
									"ALTER TABLE events_pa_link DROP COLUMN pa_link_manual_mailing_address");
						}
					}
					if (structureVersion == 12)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (columnExists(con, "events_category", "category_desc"))
						{
							ok = executeSqlQuery(con, "ALTER TABLE events_category DROP COLUMN category_desc");
						}
						if (!columnExists(con, "events_category", "category_description"))
						{
							ok = executeSqlQuery(con, "ALTER TABLE events_category ADD COLUMN category_description");
						}
					}
					if (structureVersion == 13)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						stm.execute("DELETE FROM events_address_group_member WHERE ISNULL(address_group_member_address_group_id)");
						stm.execute("DELETE FROM events_address_group_member WHERE ISNULL(address_group_member_address_id)");

						Statement upd = null;
						long agId = 0;
						long addressGroupId = 0;
						long adId = 0;
						long addressPersoId = 0;
						rst = stm
								.executeQuery("SELECT address_group_member_id, address_group_member_address_group_id, address_group_member_pa_link_id FROM events_address_group_member WHERE (NOT ISNULL(address_group_member_address_group_id)) AND (NOT ISNULL(address_group_member_pa_link_id)) AND address_group_member_deleted = 0 ORDER BY address_group_member_address_group_id, address_group_member_pa_link_id");
						while (rst.next())
						{
							addressGroupId = rst.getLong("address_group_member_address_group_id");
							addressPersoId = rst.getLong("address_group_member_pa_link_id");
							if (addressGroupId == agId && addressPersoId == adId)
							{
								if (upd == null)
								{
									upd = con.createStatement();
								}
								upd.execute("UPDATE events_address_group_member SET address_group_member_deleted = 1 WHERE address_group_member_id = "
										+ String.valueOf(rst.getLong("address_group_member_id")));
							}
							agId = rst.getLong("address_group_member_address_group_id");
							adId = rst.getLong("address_group_member_pa_link_id");
						}
						agId = 0;
						addressGroupId = 0;
						adId = 0;
						addressPersoId = 0;
						rst = stm
								.executeQuery("SELECT address_group_member_id, address_group_member_address_group_id, address_group_member_address_id FROM events_address_group_member WHERE (NOT ISNULL(address_group_member_address_group_id)) AND (NOT ISNULL(address_group_member_address_id)) AND ISNULL(address_group_member_pa_link_id) AND address_group_member_deleted = 0 ORDER BY address_group_member_address_group_id, address_group_member_address_id");
						while (rst.next())
						{
							addressGroupId = rst.getLong("address_group_member_address_group_id");
							addressPersoId = rst.getLong("address_group_member_address_id");
							if (addressGroupId == agId && addressPersoId == adId)
							{
								if (upd == null)
								{
									upd = con.createStatement();
								}
								upd.execute("UPDATE events_address_group_member SET address_group_member_deleted = 1 WHERE address_group_member_id = "
										+ String.valueOf(rst.getLong("address_group_member_id")));
							}
							agId = rst.getLong("address_group_member_address_group_id");
							adId = rst.getLong("address_group_member_address_id");
						}
						if (upd != null)
						{
							upd.close();
						}
					}
					if (structureVersion == 16)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_participant", "participant_count"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_participant ").append(
											"ADD COLUMN participant_count INTEGER DEFAULT 1").toString());
							ok = executeSqlQuery(con,
									"UPDATE events_participant SET participant_count = 1 WHERE participant_count = 0");
						}
						if (!columnExists(con, "events_domain", "domain_organization_name"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_domain ").append(
											"ADD COLUMN domain_organization_name VARCHAR(255) DEFAULT \"\"").toString());
						}
						if (!columnExists(con, "events_domain", "domain_organization_address"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_domain ").append(
											"ADD COLUMN domain_organization_address VARCHAR(255) DEFAULT \"\"")
											.toString());
						}
						if (!columnExists(con, "events_domain", "domain_organization_city"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_domain ").append(
											"ADD COLUMN domain_organization_city VARCHAR(255) DEFAULT \"\"").toString());
						}
						if (!columnExists(con, "events_domain", "domain_organization_phone"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_domain ").append(
											"ADD COLUMN domain_organization_phone VARCHAR(255) DEFAULT \"\"")
											.toString());
						}
						if (!columnExists(con, "events_domain", "domain_organization_fax"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_domain ").append(
											"ADD COLUMN domain_organization_fax VARCHAR(255) DEFAULT \"\"").toString());
						}
						if (!columnExists(con, "events_domain", "domain_organization_email"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_domain ").append(
											"ADD COLUMN domain_organization_email VARCHAR(255) DEFAULT \"\"")
											.toString());
						}
						if (!columnExists(con, "events_domain", "domain_organization_website"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_domain ").append(
											"ADD COLUMN domain_organization_website VARCHAR(255) DEFAULT \"\"")
											.toString());
						}
					}
					if (structureVersion == 17)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						ok = executeSqlQuery(con,
								"UPDATE events_participant SET participant_count = 1 WHERE participant_count = 0");
					}
					if (structureVersion == 18)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_person_settings",
								"person_settings_add_blank_after_point_in_city"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_person_settings ")
											.append("ADD COLUMN person_settings_add_blank_after_point_in_city SMALLINT DEFAULT 0")
											.toString());
							ok = executeSqlQuery(con,
									"UPDATE events_person_settings SET person_settings_add_blank_after_point_in_city = 0");
						}
					}
					if (structureVersion == 19)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_address_salutation",
								"address_salutation_show_address_name_for_person"))
						{
							ok = executeSqlQuery(
									con,
									new StringBuilder("ALTER TABLE events_address_salutation ")
											.append("ADD COLUMN address_salutation_show_address_name_for_person SMALLINT DEFAULT 0")
											.toString());
						}
					}
					if (structureVersion == 20)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						String selectSql = "SELECT l.pa_link_address_id AS address_id, p.person_another_line AS another_line FROM events_pa_link l, events_person p WHERE l.pa_link_person_id = p.person_id";
						Statement selectStatement = con.createStatement();
						ResultSet resultSet = selectStatement.executeQuery(selectSql);
						int i = 0;
						while (resultSet.next())
						{
							Statement updateStatement = con.createStatement();
							updateStatement.executeUpdate("UPDATE events_address SET address_another_line = '"
									+ resultSet.getString("another_line") + "' WHERE address_id = "
									+ resultSet.getLong("address_id"));
							System.out.println(++i);
						}
					}
					if (structureVersion == 21)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_address_salutation", "address_salutation_name"))
						{
							ok = executeSqlQuery(con, new StringBuilder("ALTER TABLE events_address_salutation ")
									.append("ADD COLUMN address_salutation_name VARCHAR(255)").toString());
							ok = executeSqlQuery(con,
									"UPDATE events_address_salutation SET address_salutation_name = address_salutation_salutation");
						}
					}
					if (structureVersion == 22)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						ok = executeSqlQuery(
								con,
								"update events_sequence set seq_name = 'events_donation_purpose_id_seq' where seq_name = 'events_donation_donation_purpose_id_seq'");
					}
					if (structureVersion == 23)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!tableExists(con, "events_payment_term"))
						{
							StringBuilder builder = new StringBuilder(
									"CREATE TABLE IF NOT EXISTS events_payment_term (");
							builder.append("payment_term_id BIGINT NOT NULL,");
							builder.append("payment_term_text LONGTEXT DEFAULT NULL,");
							builder.append("payment_term_deleted tinyint(3) unsigned DEFAULT NULL,");
							builder.append("payment_term_version smallint(5) DEFAULT NULL,");
							builder.append("payment_term_user_id int(10) DEFAULT NULL,");
							builder.append("payment_term_updated datetime DEFAULT NULL,");
							builder.append("payment_term_inserted datetime DEFAULT NULL,");
							builder.append("PRIMARY KEY (payment_term_id),");
							builder.append("FOREIGN KEY (payment_term_user_id) REFERENCES events_user (user_id)");
							builder.append(") " + getEngine() + " " + getCharset() + ";");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
						if (!rowExists(con, "events_sequence", "seq_name", "events_payment_term_id_seq", "'"))
						{
							String sql = "INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_payment_term_id_seq', 1);";
							log(LogService.LOG_INFO, sql);
							System.out.println(sql);
							ok = executeSqlQuery(con,
									"INSERT INTO events_sequence (seq_name, seq_count) VALUES ('events_payment_term_id_seq', 1);");
						}
						if (!columnExists(con, "events_booking", "booking_payment_term_id"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_booking ");
							builder.append("ADD COLUMN booking_payment_term_id bigint NULL,");
							builder.append("ADD FOREIGN KEY booking_payment_term_id (booking_payment_term_id) REFERENCES events_payment_term (payment_term_id);");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
					}
					if (structureVersion == 24)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_course", "course_payment_term_id"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_course ");
							builder.append("ADD COLUMN course_payment_term_id bigint NULL,");
							builder.append("ADD FOREIGN KEY course_payment_term_id (course_payment_term_id) REFERENCES events_payment_term (payment_term_id);");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
					}
					if (structureVersion == 25)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_guide_type", "guide_type_template"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_guide_type ");
							builder.append("ADD COLUMN guide_type_template VARCHAR(255) NULL");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
						if (!columnExists(con, "events_course", "course_prerequisites"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_course ");
							builder.append("ADD COLUMN course_prerequisites " + getClobTypeName() + " NULL");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
						if (!columnExists(con, "events_compensation_type", "compensation_type_type"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_compensation_type ");
							builder.append("ADD COLUMN compensation_type_type INTEGER NULL");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
					}
					if (structureVersion == 26)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_course_guide", "course_guide_note"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_course_guide ");
							builder.append("ADD COLUMN course_guide_note " + this.getClobTypeName() + " NULL");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
					}
					if (structureVersion == 27)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_address", "address_notes"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_address ");
							builder.append("ADD COLUMN address_notes " + this.getClobTypeName() + " NULL");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
					}
					if (structureVersion == 28)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_course", "course_cost_note"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_course ");
							builder.append("ADD COLUMN course_cost_note " + this.getClobTypeName() + " NULL");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
					}
					if (structureVersion == 29)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						if (!columnExists(con, "events_guide_type", "guide_type_use_in_prints"))
						{
							StringBuilder builder = new StringBuilder("ALTER TABLE events_guide_type ");
							builder.append("ADD COLUMN guide_type_use_in_prints SMALLINT NOT NULL DEFAULT 0");
							log(LogService.LOG_INFO, builder.toString());
							System.out.println(builder.toString());
							ok = executeSqlQuery(con, builder.toString());
						}
					}
					if (structureVersion == 30)
					{
						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						StringBuilder builder = new StringBuilder("UPDATE events_zip_code SET zip_code_country_id = 125 WHERE zip_code_state = 'FL'");
						log(LogService.LOG_INFO, builder.toString());
						System.out.println(builder.toString());
						ok = executeSqlQuery(con, builder.toString());

						log(LogService.LOG_INFO, "Updating structure version to " + structureVersion + 1);
						builder = new StringBuilder("UPDATE events_address a LEFT JOIN events_zip_code z ON a.address_zip = z.zip_code_zip SET a.address_zip_code_id = z.zip_code_id, a.address_country_id = z.zip_code_country_id WHERE a.address_country_id = 212 OR a.address_country_id = 125");
						log(LogService.LOG_INFO, builder.toString());
						System.out.println(builder.toString());
						ok = executeSqlQuery(con, builder.toString());
					}

					stm.execute("UPDATE events_version SET version_structure = " + ++structureVersion);
				}
			}
		}
		catch (SQLException e)
		{
			result = ResultType.EXIT_PROGRAM;
			result.setMessage("Beim Versuch, die Datenbankstruktur zu aktualisieren ist ein Fehler aufgetreten. Bitte setzen Sie sich mit dem Hersteller des Programms in Verbindung.");
			result.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}
		finally
		{
			try
			{
				if (stm != null)
					stm.close();
			}
			catch (SQLException e)
			{
			}
		}
		return result;
	}

	public static DatabaseUpdater newInstance(final String driver)
	{
		if (driver.equals("com.mysql.jdbc.Driver"))
		{
			return new MysqlDatabaseUpdater();
		}
		return null;
	}
}
