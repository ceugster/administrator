package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseDetailQuery extends AbstractEntityQuery<CourseDetail>
{
	public CourseDetailQuery(final ConnectionService service)
	{
		super(service);
	}
}
