package ch.eugster.events.course.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;

import ch.eugster.events.documents.maps.CourseMap;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.Season;

public class CourseDescriptionFactory
{
	private List<CourseMap> courses = new ArrayList<CourseMap>();

	private IStructuredSelection ssel;

	private Map<CourseState, Boolean> states;

	private CourseDescriptionFactory(final IStructuredSelection ssel, Map<CourseState, Boolean> states)
	{
		this.ssel = ssel;
		this.states = states;
		this.setSelection();
	}
	
	public void setCourses(CourseMap[] courseMaps)
	{
		courses = Arrays.asList(courseMaps);
	}

	public CourseMap[] getCourses()
	{
		return courses.toArray(new CourseMap[0]);
	}

	private int setSelection()
	{
		Map<Long, Course> courses = new HashMap<Long, Course>();
		Object[] objects = this.ssel.toArray();
		for (Object object : objects)
		{
			if (object instanceof Season)
			{
				Season season = (Season) object;
				for (Course course : season.getCourses())
				{
					addCourse(course, courses, states);
				}
			}
			else if (object instanceof Course)
			{
				Course course = (Course) object;
				addCourse(course, courses, states);
			}
		}
		return size();
	}

	private void addCourse(Course course, Map<Long, Course> courses, Map<CourseState, Boolean> states)
	{
		if (!course.isDeleted())
		{
			if (courses.get(course.getId()) == null)
			{
				Boolean state = states.get(course.getState());
				if (state != null && state.booleanValue())
				{
					courses.put(course.getId(), course);
					this.courses.add(new CourseMap(course));
				}
			}
		}
	}

	public int size()
	{
		return this.courses.size();
	}

	public static CourseDescriptionFactory create(final IStructuredSelection ssel,
			Map<CourseState, Boolean> states)
	{
		return new CourseDescriptionFactory(ssel, states);
	}
}
