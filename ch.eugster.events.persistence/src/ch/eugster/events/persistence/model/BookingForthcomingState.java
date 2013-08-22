package ch.eugster.events.persistence.model;

public enum BookingForthcomingState implements IBookingState
{
	BOOKED, WAITING_LIST, PROVISIONAL_BOOKED, BOOKING_CANCELED;

	private int count;

	public void add(int count)
	{
		this.count = this.count + count;
	}

	public int getCount()
	{
		return this.count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	@Override
	public String toString()
	{
		switch (this)
		{
			case BOOKED:
				return "Angemeldet";
			case WAITING_LIST:
				return "Warteliste";
			case PROVISIONAL_BOOKED:
				return "Provisorisch";
			case BOOKING_CANCELED:
				return "Annulliert";
			default:
				return "";
		}
	}

	@Override
	public int compareTo(IBookingState otherBookingState)
	{
		return this.ordinal() - otherBookingState.ordinal();
	}
}
