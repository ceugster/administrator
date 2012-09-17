package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.persistence.service.ConnectionService;

public class TeacherQuery extends AbstractEntityQuery<Teacher>
{
	public TeacherQuery(ConnectionService service)
	{
		super(service);
	}
}
