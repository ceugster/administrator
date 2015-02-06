package ch.eugster.events.persistence.queries;

import java.util.List;

import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.service.ConnectionService;

public class GuideQuery extends AbstractEntityQuery<Guide>
{
	public GuideQuery(final ConnectionService service)
	{
		super(service);
	}

	public List<Guide> selectAll()
	{
		return this.selectAll(Guide.class);
	}
}
