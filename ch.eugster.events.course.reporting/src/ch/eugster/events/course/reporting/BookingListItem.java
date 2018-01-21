package ch.eugster.events.course.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.BookingType;
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
//
//	private int didNotParticipate;
	
	private double amount;

	private double payed;

	private String status;
	
	private Map<String, Integer> countForBookingTypes = new HashMap<String, Integer>();

	/**
	 * Load Address
	 * 
	 * @param member
	 */
	public BookingListItem(final Course course, Map<String, BookingTypeKey> bookingTypeKeys)
	{
		this.course = course;
		loadData(course, bookingTypeKeys);
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
	
	public Map<String, Integer> getBookingTypeCounts()
	{
		return this.countForBookingTypes;
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

	private void loadData(final Course course, Map<String, BookingTypeKey> bookingTypeKeys)
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
//		List<BookingType> courseBookingTypes = course.getBookingTypes();
//		for (BookingType courseBookingType : courseBookingTypes)
//		{
//			BookingTypeKey key = new BookingTypeKey(courseBookingType.getName(), courseBookingType.getName(), courseBookingType.getComboLabel());
//			countForBookingTypes.put(key, Integer.valueOf(0));
//		}
		List<Booking> bookings = course.getBookings();
		for (Booking booking : bookings)
		{
			this.amount += booking.getAmount(new IBookingState[] { BookingForthcomingState.BOOKED, BookingDoneState.PARTICIPATED });
			this.payed += booking.getPayAmount();

			if (course.getState().equals(CourseState.FORTHCOMING))
			{
				if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.BOOKED))
				{
					this.booked += booking.getParticipantCount();
					List<BookingType> courseBookingTypes = course.getBookingTypes();
					for (BookingType bookingType : courseBookingTypes)
					{
						BookingTypeKey key = new BookingTypeKey(booking.getBookingState(booking.getCourse().getState()) + " " + bookingType.getName(), booking.getBookingState(booking.getCourse().getState()) + " " + bookingType.getName(), booking.getBookingState(booking.getCourse().getState()) + " " + bookingType.getComboLabel());
						Integer count = countForBookingTypes.get(key.getKey());
						count = count == null ? Integer.valueOf(0) : count;
						int counter = booking.getParticipantCount(bookingType);
						if (counter > 0) 
						{
							countForBookingTypes.put(key.getKey(), Integer.valueOf(count.intValue() + counter));
							bookingTypeKeys.put(key.getKey(), key);
						}
					}
				}
				else if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.WAITING_LIST))
				{
					this.waitingList += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingForthcomingState.PROVISIONAL_BOOKED))
				{
					this.provisional += booking.getParticipantCount();
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
					this.booked += booking.getParticipantCount();
					List<BookingType> courseBookingTypes = course.getBookingTypes();
					for (BookingType bookingType : courseBookingTypes)
					{
						BookingTypeKey key = new BookingTypeKey(booking.getBookingState(booking.getCourse().getState()) + " " + bookingType.getName(), booking.getBookingState(booking.getCourse().getState()) + " " + bookingType.getName(), booking.getBookingState(booking.getCourse().getState()) + " " + bookingType.getComboLabel());
						Integer count = countForBookingTypes.get(key.getKey());
						count = count == null ? Integer.valueOf(0) : count;
						int counter = booking.getParticipantCount(bookingType);
						if (counter > 0) 
						{
							countForBookingTypes.put(key.getKey(), Integer.valueOf(count.intValue() + counter));
							bookingTypeKeys.put(key.getKey(), key);
						}
					}
				}
				else if (booking.getBookingState(course.getState()).equals(BookingDoneState.PARTICIPATION_BROKE_OFF))
				{
					this.waitingList += booking.getParticipantCount();
				}
				else if (booking.getBookingState(course.getState()).equals(BookingDoneState.NOT_PARTICIPATED))
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
