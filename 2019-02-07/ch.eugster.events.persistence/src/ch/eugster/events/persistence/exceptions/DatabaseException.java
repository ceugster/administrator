package ch.eugster.events.persistence.exceptions;

public class DatabaseException extends Exception {

	public static final long serialVersionUID = 9000001l;
	
	public DatabaseException() {
		super();
	}
	
	public DatabaseException(Throwable throwable) {
		super(throwable);
	}
	
	public DatabaseException(String msg, Throwable throwable) 
	{
		super(msg, throwable);
	}
}
