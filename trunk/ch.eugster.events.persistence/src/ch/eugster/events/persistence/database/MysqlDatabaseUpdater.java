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
	
	protected String getCreateTable(String tableName, String[] columnNames, String[] dataTypes, String[] defaults, String primaryKey, String[] indexKeys)
	{
		StringBuilder sql = new StringBuilder("CREATE TABLE `" + tableName + "` (\n");
		for (int i = 0; i < columnNames.length; i++)
		{
			sql = sql.append(" `" + columnNames[i] + "` " + dataTypes[i] + " " + defaults[i] + ",\n");
		}
		sql = sql.append(" PRIMARY KEY (`" + primaryKey + "`) " + (indexKeys.length == 0 ? "" : ",\n"));
		for (int i = 0; i < indexKeys.length; i++)
		{
			sql = sql.append(" KEY `" + indexKeys[i] + "` (`" + indexKeys[i] + "`)");
			if (i < indexKeys.length - 1)
			{
				sql = sql.append(",\n");
			}
		}
		sql = sql.append(")\nENGINE=InnoDB");
		return sql.toString();
	}
}
