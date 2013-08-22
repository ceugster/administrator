package ch.eugster.events.persistence.model;

public interface IBookingState
{
	void add(int count);

	int getCount();

	String name();

	void setCount(int count);

	int ordinal();

	int compareTo(IBookingState otherBookingState);
}
