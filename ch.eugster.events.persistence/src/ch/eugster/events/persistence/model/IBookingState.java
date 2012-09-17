package ch.eugster.events.persistence.model;

public interface IBookingState
{
	void add(int count);
	int getCount();
	void setCount(int count);
}
