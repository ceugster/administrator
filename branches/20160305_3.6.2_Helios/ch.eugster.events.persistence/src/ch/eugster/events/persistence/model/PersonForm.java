package ch.eugster.events.persistence.model;

public enum PersonForm
{
	POLITE, PERSONAL;

	@Override
	public String toString()
	{
		if (this.equals(POLITE))
		{
			return "h�flich";
		}
		else if (this.equals(PERSONAL))
		{
			return "pers�nlich";
		}
		else
			throw new RuntimeException("Ung�ltige Anredeform");
	}
}
