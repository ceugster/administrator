package ch.eugster.events.course.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.Participant;

public class ParticipantListFactory
{
	private Collection<ParticipantListReportItem> participantListReportItems = new ArrayList<ParticipantListReportItem>();

	private Course course;

	private ParticipantListFactory(final Course course)
	{
		this.setCourse(course);
	}

	public Map<String, Object> getParticipantListReportParameters()
	{
		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("organization", course.getDomain() == null ? "" : course.getDomain().getOrganization());
		parameters.put("address", course.getDomain() == null ? "" : course.getDomain().getAddress());
		parameters.put("city", course.getDomain() == null ? "" : course.getDomain().getCity());
		parameters.put("phone", course.getDomain() == null ? "" : course.getDomain().getPhone());
		parameters.put("email", course.getDomain() == null ? "" : course.getDomain().getEmail());
		parameters.put("fax", course.getDomain() == null ? "" : course.getDomain().getFax());
		parameters.put("website", course.getDomain() == null ? "" : course.getDomain().getWebsite());
		parameters.put("header", "Teilnehmerliste");

		parameters.put("code", course.getCode());
		parameters.put("title", course.getTitle() + " (" + course.getState().toString() + ")");

		StringBuilder builder = new StringBuilder();
		Collection<CourseGuide> guides = course.getCourseGuides();
		for (CourseGuide guide : guides)
		{
			builder = builder.append(guide.getGuideType().getName() + ": "
					+ PersonFormatter.getInstance().formatFirstnameLastname(guide.getGuide().getLink().getPerson())
					+ ", ");
			builder = builder.append(AddressFormatter.getInstance().formatAddressLine(
					guide.getGuide().getLink().getAddress())
					+ ", ");
			builder = builder.append(AddressFormatter.getInstance().formatCityLine(
					guide.getGuide().getLink().getAddress())
					+ ", ");
			builder = builder.append(LinkPersonAddressFormatter.getInstance().formatPhoneWithOptionalPrefix(
					guide.getGuide().getLink().getPerson().getCountry(), guide.getGuide().getLink().getPhone())
					+ "\n");
		}
		parameters.put("guidance", builder.toString());

		builder = new StringBuilder();
		builder = builder.append(CourseFormatter.getInstance().formatMediumDate(course));
		parameters.put("dates", builder.toString());

		Map<IBookingState, Integer> counts = new HashMap<IBookingState, Integer>();
		Collection<Booking> bookings = course.getBookings();
		for (Booking booking : bookings)
		{
			Integer count = counts.get(booking.getBookingState(course.getState()));
			if (count == null)
			{
				count = new Integer(booking.getParticipantCount());
			}
			else
			{
				int c = count.intValue();
				c += booking.getParticipantCount();
				count = new Integer(c);
			}
			counts.put(booking.getBookingState(course.getState()), count);
		}

		int max = Math.max(BookingForthcomingState.values().length, BookingDoneState.values().length);
		max = Math.max(max, BookingAnnulatedState.values().length);
		for (int i = 0; i < max; i++)
		{
			parameters.put("bookingTypeKey" + i, null);
			parameters.put("bookingTypeValue" + i, null);
		}
		int i = 0;
		Set<Entry<IBookingState, Integer>> bookingStates = counts.entrySet();
		for (Entry<IBookingState, Integer> bookingState : bookingStates)
		{
			parameters.put("bookingTypeKey" + i, bookingState.getKey().toString());
			parameters.put("bookingTypeValue" + i, bookingState.getValue());
			i++;
		}
		return parameters;
	}

	public ParticipantListReportItem[] getParticipants()
	{
		return participantListReportItems.toArray(new ParticipantListReportItem[0]);
	}

	public int setCourse(final Course course)
	{
		this.course = course;
		Collection<Booking> bookings = course.getBookings();
		for (Booking booking : bookings)
		{
			Collection<Participant> participants = booking.getParticipants();
			for (Participant participant : participants)
			{
				this.participantListReportItems.add(new ParticipantListReportItem(participant));
			}
		}
		return size();
	}

	public int size()
	{
		return this.participantListReportItems.size();
	}

	public static ParticipantListFactory create(final Course course)
	{
		return new ParticipantListFactory(course);
	}
}
