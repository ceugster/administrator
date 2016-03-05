/*
 * Created on 07.01.2009
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.persistence.exceptions;


public class PersistenceException extends Exception
{
	public static final long serialVersionUID = 0l;

	private Type type;

	private ErrorCode errorCode;

	private String message;

	private Exception parent;

	public PersistenceException(Exception e)
	{
		parent = e;
		// if (e instanceof SQLException)
		// {
		// SQLException ex = (SQLException) e;
		// if (ex.getSQLState().length() >= 2)
		// {
		// if (ex.getSQLState().startsWith("08"))
		// {
		// this.type = Type.NO_CONNECTION;
		// this.errorCode = PersistenceException.ErrorCode.CONNECTION_FAILOR;
		// }
		// else if (ex.getSQLState().startsWith("42"))
		// {
		// this.type = Type.SQL;
		// this.errorCode = PersistenceException.ErrorCode.INVALID_TABLE;
		// }
		// }
		// else if (ex.getErrorCode() == 4002)
		// {
		// this.type = Type.NO_CONNECTION;
		// this.errorCode = PersistenceException.ErrorCode.SQL_4002;
		// }
		// else if (ex.getErrorCode() == 5006)
		// {
		// this.type = Type.SQL;
		// this.errorCode = PersistenceException.ErrorCode.SQL_5006;
		// }
		// else if (ex.getSQLState().equals("28000"))
		// {
		// this.type = Type.NO_CONNECTION;
		// this.errorCode = PersistenceException.ErrorCode.SQL_28000;
		// }
		// else
		// {
		// this.type = Type.NO_CONNECTION;
		// this.errorCode = PersistenceException.ErrorCode.SQL_UNKNOWN;
		// }
		// }
		// else if (e instanceof DatabaseException)
		// {
		// this.type = null;
		// this.errorCode = PersistenceException.ErrorCode.DB_UNKNOWN;
		// }
		// else if (e instanceof ClassNotFoundException)
		// {
		// this.type = Type.NO_CONNECTION;
		// this.errorCode = ErrorCode.DRIVER_NOT_FOUND;
		// }
		// else if (e instanceof OptimisticLockException)
		// {
		// this.type = Type.OPTIMISTIC_LOCK;
		// this.errorCode = ErrorCode.LCK_UNKNOWN;
		// }
	}

	public PersistenceException(ErrorCode errorCode)
	{
		this.errorCode = errorCode;
	}

	public Exception getParent()
	{
		return parent;
	}

	public Type getType()
	{
		return type;
	}

	public ErrorCode getErrorCode()
	{
		return errorCode;
	}

	@Override
	public String getMessage()
	{
		if (errorCode.equals(ErrorCode.UNKNOWN))
		{
			return message == null ? "" : message;
		}
		else
			return errorCode.toString();
	}

	@Override
	public String getLocalizedMessage()
	{
		return getMessage();
	}

	public enum Type
	{
		NO_CONNECTION, OPTIMISTIC_LOCK, SQL;
	}

	public enum SQLErrorCode
	{
		SQL_07, SQL_08, SQL_42;

		public ErrorCode internalCode()
		{
			return ErrorCode.UNKNOWN;
		}
	}

	public enum ErrorCode
	{

		UNKNOWN, DRIVER_NOT_FOUND, DRIVER_NOT_VALID, CONNECTION_FAILOR, DATABASE_NOT_INITIALIZED, DATABASE_VERSION_ERROR;

		@Override
		public String toString()
		{
			// if (this.equals(ErrorCode.SQL_4002)) return "";
			// if (this.equals(ErrorCode.SQL_5006)) return "";
			// if (this.equals(ErrorCode.SQL_28000))
			// return
			// "Falscher Benutzername oder falsches Passwort. Der Zugriff auf die Datenbank wurde verweigert.";
			if (equals(ErrorCode.UNKNOWN))
				return "Ein nicht näher spezifizierter Datenbankfehler ist aufgetreten.";
			if (equals(ErrorCode.DRIVER_NOT_FOUND))
				return "Der Treiber für die Datenbankverbindung konnte nicht gefunden werden.";
			if (equals(ErrorCode.DRIVER_NOT_VALID))
				return "Der verwendete Treiber ist für die gewählte Datenbankverbindung unbrauchbar.";
			if (equals(ErrorCode.CONNECTION_FAILOR))
				return "Es konnte keine Verbindung zur Datenbank hergestellt werden.";
			if (equals(ErrorCode.DATABASE_NOT_INITIALIZED))
				return "Die Datenbank ist nicht für ColibriTS eingerichtet.";
			if (equals(ErrorCode.DATABASE_VERSION_ERROR))
				return "Die Datenbank ist aktueller als das Programm. Bitte aktualisieren Sie das Programm.";
			return "";
		}
	}
}
