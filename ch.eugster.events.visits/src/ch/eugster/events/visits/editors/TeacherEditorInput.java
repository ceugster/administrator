package ch.eugster.events.visits.editors;

import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class TeacherEditorInput extends AbstractEntityEditorInput<Teacher>
{
	public TeacherEditorInput(Teacher teacher)
	{
		entity = teacher;
	}

	@Override
	public boolean hasParent()
	{
		return false;
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
}
