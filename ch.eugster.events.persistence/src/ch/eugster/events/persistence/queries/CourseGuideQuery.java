package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseGuideQuery extends AbstractEntityQuery<CourseGuide>
{
	public CourseGuideQuery(final ConnectionService service)
	{
		super(service);
	}
}
