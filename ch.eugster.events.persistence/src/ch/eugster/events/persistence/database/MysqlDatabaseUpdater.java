package ch.eugster.events.persistence.database;

public class MysqlDatabaseUpdater extends DatabaseUpdater
{
	protected String getClobTypeName()
	{
		return "LONGTEXT";
	}
}
