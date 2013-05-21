package ch.eugster.events.course.reporting;

import java.util.Collection;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;

public class BookingListReportItem implements Comparable<BookingListReportItem>
{
	private String code;

	private String name;

	private String state;

	private int booked;

	private int waitingList;

	private int provisional;

	private int canceled;

	private int participated;

	private int brokeOff;

	private int notParticipated;

	private double amount;

	private double payed;

	public BookingListReportItem()
	{
		super();
	}

	/**
	 * Load Address
	 * 
	 * @param member
	 */
	public BookingListReportItem(final Course course)
	{
		loadData(course);
	}

	@Override
	public int compareTo(final BookingListReportItem other)
	{
		BookingListReportItem item = other;
		int comparison = this.getName().compareTo(item.getName());
		if (comparison == 0)
		{
			return this.getCode().compareTo(item.getCode());
		}
		return comparison;
	}

	public Double getAmount()
	{
		return amount;
	}

	public Integer getBooked()
	{
		return Integer.valueOf(booked);
	}

	public Integer getCanceled()
	{
		return Integer.valueOf(canceled);
	}

	public String getCode()
	{
		return code;
	}

	public Double getDue()
	{
		return this.amount - this.payed;
	}

	public String getName()
	{
		return name;
	}

	public Double getPayed()
	{
		return payed;
	}

	public Integer getProvisional()
	{
		return Integer.valueOf(provisional);
	}

	public String getState()
	{
		return state;
	}

	public Integer getWaitingList()
	{
		return Integer.valueOf(waitingList);
	}

	private void loadData(final Course course)
	{
		this.amount = 0D;
		this.canceled = 0;
		this.booked = 0;
		this.payed = 0D;
		this.provisional = 0;
		this.waitingList = 0;

		this.code = course.getCode();
		this.name = course.getTitle();
		this.state = course.getState().toString();

		Collection<Booking> bookings = course.getBookings();
		for (Booking booking : bookings)
		{
			this.amount += booking.getAmount();
			this.payed += booking.getPayAmount();

			if (course.getState().equals(CourseState.FORTHCOMING))
			{
				if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.BOOKED))
				{
					booked += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.WAITING_LIST))
				{
					waitingList += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.PROVISIONAL_BOOKED))
				{
					provisional += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.BOOKING_CANCELED))
				{
					canceled += booking.getParticipantCount();
				}
			}
			else if (course.getState().equals(CourseState.DONE))
			{
				if (booking.getBookingState(course.getState()).equals(BookingDoneState.PARTICIPATED))
				{
					participated += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingDoneState.PARTICIPATION_BROKE_OFF))
				{
					brokeOff += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.BOOKING_CANCELED))
				{
					notParticipated += booking.getParticipantCount();
				}
			}
			else if (course.getState().equals(CourseState.ANNULATED))
			{
				if (booking.getBookingState(course.getState()).equals(BookingAnnulatedState.ANNULATED))
				{
					canceled += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingAnnulatedState.COURSE_CANCELED))
				{
					canceled += booking.getParticipantCount();
				}
			}
		}
	}
}
