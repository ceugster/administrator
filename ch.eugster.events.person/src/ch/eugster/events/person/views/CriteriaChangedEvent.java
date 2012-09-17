package ch.eugster.events.person.views;

import ch.eugster.events.persistence.model.AbstractEntity;

public class CriteriaChangedEvent
{
	private final AbstractEntity[] result;

	public CriteriaChangedEvent(AbstractEntity[] result)
	{
		this.result = result;
	}

	public AbstractEntity[] getResult()
	{
		return this.result;
	}
}
