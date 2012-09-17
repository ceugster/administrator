package ch.eugster.events.persistence.model;

public enum CourseState 
{
	FORTHCOMING,
	DONE,
	ANNULATED;

	String[] states = new String[] { "Bevorstehend", "Durchgeführt", "Annuliert" };

	@Override
	public String toString() {
		return this.states[this.ordinal()];
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
}
