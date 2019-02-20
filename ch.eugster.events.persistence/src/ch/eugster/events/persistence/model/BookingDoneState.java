package ch.eugster.events.persistence.model;

public enum BookingDoneState implements IBookingState
{
	PARTICIPATED, PARTICIPATION_BROKE_OFF, NOT_PARTICIPATED;

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
			case PARTICIPATED:
				return "Teilgenommen";
			case PARTICIPATION_BROKE_OFF:
				return "Teilnahme abgebrochen";
			case NOT_PARTICIPATED:
				return "Nicht teilgenommen";
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
			case PARTICIPATED:
				return "T";
			case PARTICIPATION_BROKE_OFF:
				return "G";
			case NOT_PARTICIPATED:
				return "N";
			default:
				return "";
		}
	}
}
