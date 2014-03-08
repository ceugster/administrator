package ch.eugster.events.persistence.database;

public class MysqlDatabaseUpdater extends DatabaseUpdater
{
	@Override
	protected String getClobTypeName()
	{
		return "LONGTEXT";
	}

	@Override
	protected String getCurrentDate()
	{
		return "CURRENT_DATE()";
	}

	@Override
	protected String getEngine()
	{
		return "ENGINE=InnoDB";
	}

	@Override
	protected String getCharset()
	{
		return "DEFAULT CHARSET=latin1";
	}
}
