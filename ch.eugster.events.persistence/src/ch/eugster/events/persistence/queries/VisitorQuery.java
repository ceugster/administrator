package ch.eugster.events.persistence.queries;

import java.util.List;

import ch.eugster.events.persistence.model.Visitor;
import ch.eugster.events.persistence.service.ConnectionService;

public class VisitorQuery extends AbstractEntityQuery<Visitor> 
{
	public VisitorQuery(ConnectionService service)
	{
		super(service);
	}

	public List<Visitor> selectAll(boolean deletedToo)
	{
		return super.selectAll(Visitor.class, deletedToo);
	}

}
