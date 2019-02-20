package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.AddressGroupLink;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupLinkQuery extends AbstractEntityQuery<AddressGroupLink>
{
	public AddressGroupLinkQuery(final ConnectionService service)
	{
		super(service);
	}
}
