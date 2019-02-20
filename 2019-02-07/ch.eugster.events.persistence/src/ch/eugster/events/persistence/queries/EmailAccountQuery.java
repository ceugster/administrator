package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.persistence.service.ConnectionService;

public class EmailAccountQuery extends AbstractEntityQuery<EmailAccount>
{
	public EmailAccountQuery(final ConnectionService service)
	{
		super(service);
	}
}
