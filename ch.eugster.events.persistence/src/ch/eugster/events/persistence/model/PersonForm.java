package ch.eugster.events.persistence.model;

public enum PersonForm
{
	POLITE, PERSONAL;

	@Override
	public String toString()
	{
		if (this.equals(POLITE))
		{
			return "höflich";
		}
		else if (this.equals(PERSONAL))
		{
			return "persönlich";
		}
		else
			throw new RuntimeException("Ungültige Anredeform");
	}
}
