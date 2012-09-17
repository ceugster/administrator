package ch.eugster.events.persistence.model;

public enum FieldExtensionTarget
{
	PA_LINK, PERSON;

	public String key()
	{
		switch (this)
		{
			case PA_LINK:
			{
				return "pa_link_";
			}
			case PERSON:
			{
				return "person_";
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}

	public String label()
	{
		switch (this)
		{
			case PA_LINK:
			{
				return "Person an bestimmter Adresse";
			}
			case PERSON:
			{
				return "Person";
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}
}
