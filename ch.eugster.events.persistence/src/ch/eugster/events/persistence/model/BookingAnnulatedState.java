package ch.eugster.events.persistence.model;

public enum BookingAnnulatedState implements IBookingState
{
	COURSE_CANCELED, ANNULATED;

	public int count;

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
			case COURSE_CANCELED:
				return "Kurs abgesagt";
			case ANNULATED:
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

	@Override
	public String shortName()
	{
		switch (this)
		{
			case COURSE_CANCELED:
				return "C";
			case ANNULATED:
				return "A";
			default:
				return "";
		}
	}
}
