package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.Version;
import ch.eugster.events.persistence.service.ConnectionService;

public class VersionQuery extends AbstractEntityQuery<Version>
{
	public VersionQuery(final ConnectionService service)
	{
		super(service);
	}
}
