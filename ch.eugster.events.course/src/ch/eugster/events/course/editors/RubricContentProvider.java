package ch.eugster.events.course.editors;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Rubric;

public class RubricContentProvider extends ArrayContentProvider
{
	private boolean mandatoryRubric;

	public RubricContentProvider(final boolean mandatory)
	{
		mandatoryRubric = mandatory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(final Object inputElement)
	{
		Rubric[] rubrics = new Rubric[0];
		if (mandatoryRubric)
		{
			if (inputElement instanceof Rubric[])
			{
				rubrics = (Rubric[]) inputElement;
			}
			else if (inputElement instanceof List<?>)
			{
				rubrics = ((Collection<Rubric>) inputElement).toArray(new Rubric[0]);
			}
		}
		else if (inputElement instanceof Rubric[])
		{
			Rubric[] list = (Rubric[]) inputElement;
			rubrics = new Rubric[list.length + 1];
			for (int i = 0; i < list.length; i++)
				rubrics[i + 1] = list[i];
			rubrics[0] = Rubric.newInstance();
		}
		else if (inputElement instanceof List<?>)
		{
			Rubric[] list = ((Collection<Rubric>) inputElement).toArray(new Rubric[0]);
			rubrics = new Rubric[list.length + 1];
			for (int i = 0; i < list.length; i++)
				rubrics[i + 1] = list[i];
			rubrics[0] = Rubric.newInstance();
		}
		return rubrics;
	}

}
