package ch.eugster.events.documents.test;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import ch.eugster.events.documents.maps.AbstractDataMap.WeekdayType;
import ch.eugster.events.documents.maps.CourseMap;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.Season;

public class DocumentTest 
{
	private Course createCourse(Calendar start, Calendar end)
	{
		Season season = Season.newInstance();
		Course course = Course.newInstance(season);
		CourseDetail detail = CourseDetail.getNewInstance(course);
		detail.setStart(start);
		detail.setEnd(end);
		course.addCourseDetail(detail);
		return course;
	}
	
	@Test
	public final void testDateFormatOnlyStart()
	{
		String expected = "Mo, 12. Januar 2015, 08:00";
		Calendar date = new GregorianCalendar(2015,Calendar.JANUARY, 12, 8, 0, 0);
		Course course = createCourse(date, null);
		Calendar[] dates = CourseMap.getDates(course);
		String result = CourseMap.getDateRange(dates, WeekdayType.SHORT);
		assertEquals("resulted in " + result + ", instead of " + expected, result, expected);
	}

	@Test
	public final void testDateFormatOnlyEnd()
	{
		String expected = "Mo, 12. Januar 2015, 16:00";
		Calendar date = new GregorianCalendar(2015,Calendar.JANUARY, 12, 16, 0, 0);
		Course course = createCourse(date, null);
		Calendar[] dates = CourseMap.getDates(course);
		String result = CourseMap.getDateRange(dates, WeekdayType.SHORT);
		assertEquals("resulted in " + result + ", instead of " + expected, result, expected);
	}

	@Test
	public final void testDateRangeDifferentTimes()
	{
		String expected = "Mo, 12. Januar 2015, 08:00 - 16:00";
		Calendar start = new GregorianCalendar(2015,Calendar.JANUARY, 12, 8, 0, 0);
		Calendar end = new GregorianCalendar(2015,Calendar.JANUARY, 12, 16, 0, 0);
		Course course = createCourse(start, end);
		Calendar[] dates = CourseMap.getDates(course);
		String result = CourseMap.getDateRange(dates, WeekdayType.SHORT);
		assertEquals("resulted in " + result + ", instead of " + expected, result, expected);
	}

	@Test
	public final void testDateRangeDifferentDays()
	{
		String expected = "Mo, 12. - Di, 13. Januar 2015, 08:00 - 16:00";
		Calendar start = new GregorianCalendar(2015,Calendar.JANUARY, 12, 8, 0, 0);
		Calendar end = new GregorianCalendar(2015,Calendar.JANUARY, 13, 16, 0, 0);
		Course course = createCourse(start, end);
		Calendar[] dates = CourseMap.getDates(course);
		String result = CourseMap.getDateRange(dates, WeekdayType.SHORT);
		assertEquals("resulted in " + result + ", instead of " + expected, result, expected);
	}

	@Test
	public final void testDateRangeDifferentMonths()
	{
		String expected = "Mo, 12. Januar - Fr, 13. Februar 2015, 08:00 - 16:00";
		Calendar start = new GregorianCalendar(2015,Calendar.JANUARY, 12, 8, 0, 0);
		Calendar end = new GregorianCalendar(2015,Calendar.FEBRUARY, 13, 16, 0, 0);
		Course course = createCourse(start, end);
		Calendar[] dates = CourseMap.getDates(course);
		String result = CourseMap.getDateRange(dates, WeekdayType.SHORT);
		assertEquals("resulted in " + result + ", instead of " + expected, result, expected);
	}

	@Test
	public final void testDateRangeDifferentYears()
	{
		String expected = "Mo, 12. Januar 2015 - Sa, 13. Februar 2016, 08:00 - 16:00";
		Calendar start = new GregorianCalendar(2015,Calendar.JANUARY, 12, 8, 0, 0);
		Calendar end = new GregorianCalendar(2016,Calendar.FEBRUARY, 13, 16, 0, 0);
		Course course = createCourse(start, end);
		Calendar[] dates = CourseMap.getDates(course);
		String result = CourseMap.getDateRange(dates, WeekdayType.SHORT);
		assertEquals("resulted in " + result + ", instead of " + expected, result, expected);
	}

}
