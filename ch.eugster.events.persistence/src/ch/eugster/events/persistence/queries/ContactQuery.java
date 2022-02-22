package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.Contact;
import ch.eugster.events.persistence.service.ConnectionService;

public class ContactQuery extends AbstractEntityQuery<Contact>
{
	public ContactQuery(ConnectionService service)
	{
		super(service);
	}

}
