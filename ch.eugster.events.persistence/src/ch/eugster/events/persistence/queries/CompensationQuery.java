package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.Compensation;
import ch.eugster.events.persistence.service.ConnectionService;

public class CompensationQuery extends AbstractEntityQuery<Compensation>
{
	public CompensationQuery(final ConnectionService service)
	{
		super(service);
	}
}
