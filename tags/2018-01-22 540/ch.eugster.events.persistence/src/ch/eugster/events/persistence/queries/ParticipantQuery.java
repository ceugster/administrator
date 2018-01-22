package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.service.ConnectionService;

public class ParticipantQuery extends AbstractEntityQuery<Participant>
{
	public ParticipantQuery(final ConnectionService service)
	{
		super(service);
	}
}
