package ch.eugster.events.persistence.model;

public enum CourseState
{
	FORTHCOMING, DONE, ANNULATED;

	String[] states = new String[] { "Bevorstehend", "Durchgeführt", "Annulliert" };

	String[] codes = new String[] { "B", "D", "A" };

	public IBookingState[] getBookingStates()
	{
		if (this.equals(CourseState.FORTHCOMING))
		{
			return BookingForthcomingState.values();
		}
		else if (this.equals(CourseState.DONE))
		{
			return BookingDoneState.values();
		}
		else if (this.equals(CourseState.ANNULATED))
		{
			return BookingAnnulatedState.values();
		}
		else
			throw new RuntimeException("Ungültiger Kursstatus");
	}

	public String imageKey()
	{
		if (this.equals(CourseState.FORTHCOMING))
			return "COURSE_FORTHCOMING";
		else if (this.equals(CourseState.DONE))
			return "COURSE_DONE";
		else if (this.equals(CourseState.ANNULATED))
			return "COURSE_ANNULATED";
		else
			throw new RuntimeException("Ungültiger Kursstatus");
	}

	@Override
	public String toString()
	{
		return this.states[this.ordinal()];
	}

	public String code()
	{
		return this.codes[this.ordinal()];
	}
}
