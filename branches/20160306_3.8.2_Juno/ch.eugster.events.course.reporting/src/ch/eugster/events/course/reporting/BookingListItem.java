package ch.eugster.events.course.reporting;

import java.util.Date;
import java.util.List;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.IBookingState;

public class BookingListItem implements Comparable<BookingListItem>
{
	private Course course;
	
	private String code;

	private String name;

	private Date date;

	private int min;

	private int max;

	private int booked;

	private int waitingList;

	private int provisional;

	private int canceled;

//	private int participated;
//
//	private int brokeOff;

	private double amount;

	private double payed;

	private String status;

	/**
	 * Load Address
	 * 
	 * @param member
	 */
	public BookingListItem(final Course course)
	{
		this.course = course;
		loadData(course);
	}

	@Override
	public int compareTo(final BookingListItem other)
	{
		BookingListItem item = other;
		int comparison = this.getName().compareTo(item.getName());
		if (comparison == 0)
		{
			return this.getCode().compareTo(item.getCode());
		}
		return comparison;
	}
	
	public Course getCourse()
	{
		return course;
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

	public Date getDate()
	{
		return date;
	}

	public Double getDue()
	{
		return this.amount - this.payed;
	}

	public int getMax()
	{
		return max;
	}

	public int getMin()
	{
		return min;
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

	public Integer getWaitingList()
	{
		return Integer.valueOf(waitingList);
	}

	public String getStatus()
	{
		return status;
	}

	private void loadData(final Course course)
	{
		this.amount = 0D;
		this.canceled = 0;
		this.booked = 0;
		this.payed = 0D;
		this.provisional = 0;
		this.waitingList = 0;
		this.min = course.getMinParticipants();
		this.max = course.getMaxParticipants();
		this.code = course.getCode();
		this.name = course.getTitle();
		this.date = course.getFirstDate() == null ? null : course.getFirstDate().getTime();
		this.status = course.getState().code();
		List<Booking> bookings = course.getBookings();
		for (Booking booking : bookings)
		{
			this.amount += booking.getAmount(new IBookingState[] { BookingForthcomingState.BOOKED, BookingDoneState.PARTICIPATED });
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
					booked += booking.getParticipantCount();
				}
				else if (booking.getBookingState(CourseState.FORTHCOMING).equals(BookingForthcomingState.WAITING_LIST))
				{
					waitingList += booking.getParticipantCount();
				}
				else if (booking.getBookingState(CourseState.FORTHCOMING).equals(
						BookingForthcomingState.BOOKING_CANCELED))
				{
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
