package ch.eugster.events.course.editors;

import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Rubric;

public class RubricContentProvider extends ArrayContentProvider
{
	private boolean mandatoryRubric;
	public RubricContentProvider(boolean mandatory)
	{
		mandatoryRubric = mandatory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement)
	{
		if (mandatoryRubric)
		{
			if (inputElement instanceof Rubric[])
			{
				return (Rubric[]) inputElement;
			}
			else if (inputElement instanceof Collection<?>)
			{
				return ((Collection<Rubric>) inputElement).toArray(new Rubric[0]);
			}
		}
		else
			if (inputElement instanceof Rubric[])
			{
				Rubric[] list = (Rubric[]) inputElement;
				Rubric[] rubrics = new Rubric[list.length + 1];
				for (int i = 0; i < list.length; i++)
					rubrics[i + 1] = list[i];
				rubrics[0] = Rubric.newInstance();
			}
			else if (inputElement instanceof Collection<?>)
			{
				Rubric[] list = ((Collection<Rubric>) inputElement).toArray(new Rubric[0]);
				Rubric[] rubrics = new Rubric[list.length + 1];
				for (int i = 0; i < list.length; i++)
					rubrics[i + 1] = list[i];
				rubrics[0] = Rubric.newInstance();
			}
		return new Rubric[0];
	}

}
