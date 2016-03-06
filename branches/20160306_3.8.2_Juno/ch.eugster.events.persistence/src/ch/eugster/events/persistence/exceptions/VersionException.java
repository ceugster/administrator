package ch.eugster.events.persistence.exceptions;

public class VersionException extends Exception
{
	private static final long serialVersionUID = 0l;
	
	public VersionException(Type type, String message)
	{
		super(message);
	}
	
	public enum Type
	{
			NO_RECORD, NO_TABLE;
	}
}
