package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonSettingsQuery extends AbstractEntityQuery<PersonSettings>
{
	public PersonSettingsQuery(final ConnectionService service)
	{
		super(service);
	}
}
