package ch.eugster.events.course.editors;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CourseEditorInput extends AbstractEntityEditorInput<Course>
{
	public CourseEditorInput(Course course)
	{
		this.entity = course;
	}

	@Override
	public boolean hasParent()
	{
		return true;
	}

	@Override
	public AbstractEntity getParent()
	{
		return this.entity.getSeason();
	}

	@Override
	public String getName()
	{
		return "TTT";
	}

	@Override
	public String getToolTipText()
	{

		return "FFF";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter)
	{
		if (adapter.equals(Course.class))
		{
			return this.entity;
		}
		return null;
	}
}
