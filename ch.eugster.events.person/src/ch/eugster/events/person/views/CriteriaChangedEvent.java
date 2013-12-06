package ch.eugster.events.person.views;

import ch.eugster.events.persistence.model.AbstractEntity;

public class CriteriaChangedEvent
{
	private final AbstractEntity[] result;

	public CriteriaChangedEvent(AbstractEntity[] result)
	{
		System.out.println(this);
		this.result = result;
	}

	public AbstractEntity[] getResult()
	{
		System.out.println(this);
		return this.result;
	}
}
