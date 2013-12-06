package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.Visitor;
import ch.eugster.events.persistence.service.ConnectionService;

public class VisitorQuery extends AbstractEntityQuery<Visitor> 
{
	public VisitorQuery(ConnectionService service)
	{
		super(service);
	}
}
