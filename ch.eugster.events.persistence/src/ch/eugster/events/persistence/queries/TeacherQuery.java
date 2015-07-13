package ch.eugster.events.persistence.queries;

import java.util.List;

import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.persistence.service.ConnectionService;

public class TeacherQuery extends AbstractEntityQuery<Teacher>
{
	public TeacherQuery(ConnectionService service)
	{
		super(service);
	}

	public List<Teacher> selectAll(boolean deletedToo)
	{
		return super.selectAll(Teacher.class, deletedToo);
	}

}
