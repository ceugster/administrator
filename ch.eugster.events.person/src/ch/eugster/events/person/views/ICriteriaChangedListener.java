package ch.eugster.events.person.views;

import ch.eugster.events.persistence.model.AbstractEntity;

public interface ICriteriaChangedListener
{
	void criteriaChanged(AbstractEntity[] entities);
}
