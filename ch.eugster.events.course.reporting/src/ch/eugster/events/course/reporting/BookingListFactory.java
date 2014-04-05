package ch.eugster.events.course.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;

import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;

public class BookingListFactory
{
	private Collection<BookingListReportItem> bookingListReportItems = new ArrayList<BookingListReportItem>();

	private IStructuredSelection ssel;

	private Map<CourseState, Boolean> states;

	private User user;

	private BookingListFactory(final User user, final IStructuredSelection ssel, Map<CourseState, Boolean> states)
	{
		this.user = user;
		this.ssel = ssel;
		this.states = states;
		this.setSelection();
	}

	public BookingListReportItem[] getCourses()
	{
		return bookingListReportItems.toArray(new BookingListReportItem[0]);
	}

	public Map<String, Object> getParticipantListReportParameters()
	{
		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("organization", user == null ? "" : (user.getDomain() == null ? "" : user.getDomain()
				.getOrganization()));
		parameters.put("address", user == null ? "" : (user.getDomain() == null ? "" : user.getDomain().getAddress()));
		parameters.put("city", user == null ? "" : (user.getDomain() == null ? "" : user.getDomain().getCity()));
		parameters.put("phone", user == null ? "" : (user.getDomain() == null ? "" : user.getDomain().getPhone()));
		parameters.put("email", user == null ? "" : (user.getDomain() == null ? "" : user.getDomain().getEmail()));
		parameters.put("fax", user == null ? "" : (user.getDomain() == null ? "" : user.getDomain().getFax()));
		parameters.put("website", user == null ? "" : (user.getDomain() == null ? "" : user.getDomain().getWebsite()));

		parameters.put("season", getSelectedParameter());

		return parameters;
	}

	private String getSelectedParameter()
	{
		StringBuilder builder = new StringBuilder();
		Map<Long, Course> courses = new HashMap<Long, Course>();
		Object[] objects = ssel.toArray();
		for (Object object : objects)
		{
			if (object instanceof Season)
			{
				Season season = (Season) object;
				builder.append(season.getTitle().isEmpty() ? season.getCode() : season.getTitle());
				for (Course course : season.getCourses())
				{
					if (!course.isDeleted())
					{
						if (courses.get(course.getId()) == null)
						{
							Boolean state = states.get(course.getState());
							if (state != null && state.booleanValue())
							{
								courses.put(course.getId(), course);
							}
						}
					}
				}
			}
		}
		for (Object object : objects)
		{
			if (object instanceof Course)
			{
				Course course = (Course) object;
				if (!course.isDeleted())
				{
					if (courses.get(course.getId()) == null)
					{
						Boolean state = states.get(course.getState());
						if (state != null && state.booleanValue())
						{
							builder.append(course.getTitle().isEmpty() ? course.getCode() : course.getTitle());
						}
					}
				}
			}
		}
		return builder.toString();
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
					this.bookingListReportItems.add(new BookingListReportItem(course));
				}
			}
		}
	}

	public int size()
	{
		return this.bookingListReportItems.size();
	}

	public static BookingListFactory create(final User user, final IStructuredSelection ssel,
			Map<CourseState, Boolean> states)
	{
		return new BookingListFactory(user, ssel, states);
	}
}
