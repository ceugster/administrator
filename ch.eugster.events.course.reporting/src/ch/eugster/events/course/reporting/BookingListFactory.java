package ch.eugster.events.course.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;

public class BookingListFactory
{
	private Collection<BookingListReportItem> bookingListReportItems = new ArrayList<BookingListReportItem>();

	private Season season;

	private User user;

	private BookingListFactory(final User user, final Season season)
	{
		this.user = user;
		this.setSeason(season);
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

		parameters.put("season", season.getTitle().isEmpty() ? season.getCode() : season.getTitle());

		return parameters;
	}

	public int setSeason(final Season season)
	{
		this.season = season;
		Collection<Course> courses = season.getCourses();
		for (Course course : courses)
		{
			this.bookingListReportItems.add(new BookingListReportItem(course));
		}
		return size();
	}

	public int size()
	{
		return this.bookingListReportItems.size();
	}

	public static BookingListFactory create(final User user, final Season season)
	{
		return new BookingListFactory(user, season);
	}
}
