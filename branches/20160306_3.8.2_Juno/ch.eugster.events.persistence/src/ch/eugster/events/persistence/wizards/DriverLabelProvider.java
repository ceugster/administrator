package ch.eugster.events.persistence.wizards;

import org.eclipse.jface.viewers.LabelProvider;

public class DriverLabelProvider extends LabelProvider
{
	@Override
	public String getText(final Object element)
	{
		return ((SupportedDriver) element).getPlatform();
	}

	public enum SupportedDriver
	{
		POSTGRESQL_8X, MSSQLSERVER_2008, MYSQL_51, DERBY_CS, DERBY_EMBEDDED;

		public String getDDLFileCreateOnly()
		{
			if (equals(POSTGRESQL_8X))
				return "postgresql8-create-only.sql";
			else if (equals(MSSQLSERVER_2008))
				return "mssqlserver2008.sql";
			else if (equals(MYSQL_51))
				return "mysql51.sql";
			else if (equals(DERBY_CS))
				return "derby110.sql";
			else if (equals(DERBY_EMBEDDED))
				return "derby110.sql";
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getDDLFileDropAndCreate()
		{
			if (equals(POSTGRESQL_8X))
				return "postgresql8-drop-and-create.sql";
			else if (equals(MSSQLSERVER_2008))
				return "mssqlserver2008.sql";
			else if (equals(MYSQL_51))
				return "mysql51.sql";
			else if (equals(DERBY_CS))
				return "derby110.sql";
			else if (equals(DERBY_EMBEDDED))
				return "derby110.sql";
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getDefaultURL()
		{
			if (equals(POSTGRESQL_8X))
				return "jdbc:postgresql:events";
			else if (equals(MSSQLSERVER_2008))
				return "jdbc:sqlserver:events";
			else if (equals(MYSQL_51))
				return "jdbc:mysql:events";
			else if (equals(DERBY_CS))
				return "jdbc:derby:events";
			else if (equals(DERBY_EMBEDDED))
				return "jdbc:derby:events";
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getDescription()
		{
			if (equals(POSTGRESQL_8X))
				return "Ersetzen Sie <host> durch den Namen oder die IP-Nummer des Hosts (z.B. 'localhost' oder 127.0.0.1), auf dem der Datenbankserver läuft.\nErsetzen Sie <port> durch die Nummer des Ports, über welchen die Datenbank erreichbar ist (optional, Defaultwert: 5432).\nErsetzen Sie <database> durch den Namen der Datenbank, die verwendet werden soll.";
			else if (equals(MSSQLSERVER_2008))
				return "Ersetzen Sie <host> durch den Namen oder die IP-Nummer des Hosts (z.B. 'localhost' oder 127.0.0.1), auf dem der Datenbankserver läuft.\nErsetzen Sie <instance> durch den Instanznamen der RDBMS auf dem angegebenen Host (optional).\nErsetzen Sie <port> durch die Nummer des Ports, über welchen die Datenbank erreichbar ist (optional, Defaultwert: 1433).\nErsetzen Sie [key=value]* durch Schlüssel/Wert-Paare (z.B.\n-- databaseName=<Datenbankname>\n--integratedSecurity=true\n--user=<Benutzername>\n-- password=<Passwort>\nBeachten Sie dazu die Erläuterung von Microsoft";
			else if (equals(MYSQL_51))
				return "Ersetzen Sie <host> durch den Namen oder die IP-Nummer des Hosts (z.B. 'localhost' oder 127.0.0.1), auf dem der Datenbankserver läuft.\nErsetzen Sie <port> durch die Nummer des Ports, über welchen die Datenbank erreichbar ist (optional, Defaultwert: 3306).\nErsetzen Sie <database> durch den Namen der Datenbank, die verwendet werden soll.";
			else if (equals(DERBY_CS))
				return "Ersetzen Sie <host> durch den Namen oder die IP-Nummer des Hosts, auf dem der Datenbankserver läuft.\nErsetzen Sie <port> durch die Nummer des Ports, über welchen die Datenbank erreichbar ist.\nErsetzen Sie <database> durch den Namen der Datenbank, die verwendet werden soll.\n[] = optionaler Ausdruck";
			else if (equals(DERBY_EMBEDDED))
				return "Ersetzen Sie <path> durch den (absoluten oder relativen) Pfad zum Verzeichnis, in dem sich die Datenbank befindet.\nErsetzen Sie <database> durch den Namen der Datenbank, die verwendet werden soll.\nDer Ausdruck <name=value> entspricht einem optionalen Schlüssel/Werte-Paar und kann durch entsprechende Schlüssel/Wert-Paare ersetzt werden (vgl. dazu die Dokumentation von Derby).\n[] = optionaler Ausdruck\n* = mehrfach verwendbarer Ausdruck";
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getDriver()
		{
			if (equals(POSTGRESQL_8X))
				return org.postgresql.Driver.class.getName();
			else if (equals(MSSQLSERVER_2008))
				return com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName();
			else if (equals(MYSQL_51))
				return com.mysql.jdbc.Driver.class.getName();
			else if (equals(DERBY_CS))
				return org.apache.derby.jdbc.ClientDriver.class.getName();
			else if (equals(DERBY_EMBEDDED))
				return org.apache.derby.jdbc.EmbeddedDriver.class.getName();
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getExampleURL()
		{
			if (equals(POSTGRESQL_8X))
				return "jdbc:postgresql:[//<host>[:<port>]/]<database>";
			else if (equals(MSSQLSERVER_2008))
				return "jdbc:sqlserver:[//<host>[\\instance[:<port>]][;key=value]*";
			else if (equals(MYSQL_51))
				return "jdbc:mysql:[//<host>[:<port>]/]<database>";
			else if (equals(DERBY_CS))
				return "jdbc:derby:[//<host>[:<port>]/]<database>[;<option>]";
			else if (equals(DERBY_EMBEDDED))
				return "jdbc:derby:[[/]<path>]<database>[;<name=value>]*";
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getOjbPlatform()
		{
			if (equals(POSTGRESQL_8X))
				return "PostgreSQL";
			else if (equals(MSSQLSERVER_2008))
				return "MsSQLServer";
			else if (equals(MYSQL_51))
				return "MySQL";
			else if (equals(DERBY_CS))
				return ("Derby");
			else if (equals(DERBY_EMBEDDED))
				return ("Derby");
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getOjbSubprotocol()
		{
			if (equals(POSTGRESQL_8X))
				return "postgresql";
			else if (equals(MSSQLSERVER_2008))
				return "sqlserver";
			else if (equals(MYSQL_51))
				return "mysql";
			else if (equals(DERBY_CS))
				return ("derby");
			else if (equals(DERBY_EMBEDDED))
				return ("derby");
			else
				throw new RuntimeException("Invalid driver");
		}

		public String getPlatform()
		{
			if (equals(POSTGRESQL_8X))
				return "PostgreSQL 8.x";
			else if (equals(MSSQLSERVER_2008))
				return "Microsoft SQL Server 2008 Express";
			else if (equals(MYSQL_51))
				return "MySQL 5.1";
			else if (equals(DERBY_CS))
				return ("Derby Client/Server 10.5.1.1");
			else if (equals(DERBY_EMBEDDED))
				return ("Derby Embedded 10.5.1.1");
			else
				throw new RuntimeException("Invalid driver");
		}
	}

}
