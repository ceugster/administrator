package ch.eugster.events.persistence.model;

public enum CourseSexConstraint
{
	BOTH, ONLY_FEMALE, ONLY_MALE;

	String[] sexes = new String[] { "Keine Einschränkung", "Nur weibliche Teilnehmerinnen", "Nur männliche Teilnehmer" };

	String[] symbols = new String[] { "m+w", "w", "m" };

	@Override
	public String toString()
	{
		return this.sexes[this.ordinal()];
	}

	public String toSymbol()
	{
		return this.symbols[this.ordinal()];
	}
}
