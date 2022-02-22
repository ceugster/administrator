package ch.eugster.events.persistence.exceptions;

public class OptimisticLockException extends DatabaseException
{
	public static final long serialVersionUID = 9000001l;

	public OptimisticLockException(Throwable throwable)
	{
		super(throwable);
	}

	public OptimisticLockException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
}
