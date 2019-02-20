package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.service.ConnectionService;

public class GlobalSettingsQuery extends AbstractEntityQuery<GlobalSettings>
{
	public GlobalSettingsQuery(final ConnectionService service)
	{
		super(service);
	}
}
