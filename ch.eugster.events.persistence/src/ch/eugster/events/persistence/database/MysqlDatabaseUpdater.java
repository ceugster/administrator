package ch.eugster.events.persistence.database;

public class MysqlDatabaseUpdater extends DatabaseUpdater
{
	@Override
	protected String getClobTypeName()
	{
		return "LONGTEXT";
	}

	@Override
	protected String getBlobTypeName()
	{
		return "BLOB";
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

	@Override
	protected String getCreateTable(String tableName, Column[] columns,
			String primaryKey, ForeignKey[] foreignKeys) 
	{
		StringBuilder sql = new StringBuilder("CREATE TABLE `" + tableName + "` (\n");
		for (int i = 0; i < columns.length; i++)
		{
			sql = sql.append(" `" + columns[i].name + "` " + columns[i].dataType + " " + columns[i].defaultValue + " " + (columns[i].nullable ? "NULL" : "NOT NULL") + ",\n");
		}
		sql = sql.append(" PRIMARY KEY (`" + primaryKey + "`) " + (foreignKeys.length == 0 ? "" : ",\n"));
		for (int i = 0; i < foreignKeys.length; i++)
		{
			sql = sql.append("CONSTRAINT `" + foreignKeys[i].foreignKeyName + "` FOREIGN KEY `" + foreignKeys[i].foreignKeyName + "` (`" + foreignKeys[i].columnName + "`) REFERENCES `" + foreignKeys[i].referencedTable + "` (`" + foreignKeys[i].referencedColumnName + "`)\n");
			sql = sql.append(i < foreignKeys.length - 1 ? ", " : "");
		}
		sql = sql.append(")\nENGINE=InnoDB");
		return sql.toString();
	}
}
