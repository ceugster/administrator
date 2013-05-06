package ch.eugster.events.persistence.formatters;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.persistence.model.Participant;

public class CourseFormatter
{
	private static CourseFormatter formatter = null;

	private NumberFormat bookingIdFormat = null;

	private final NumberFormat nf = NumberFormat.getCurrencyInstance();

	private final DateFormat sdf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

	public CourseFormatter()
	{
	}

	public String formatBookingId(final Booking booking)
	{
		if (this.bookingIdFormat == null)
		{
			String pattern = GlobalSettings.getInstance().getBookingIdFormat();
			this.bookingIdFormat = new DecimalFormat(pattern);
		}

		return this.bookingIdFormat.format(booking.getId());
	}

	public String formatBookingType(final BookingType bookingType)
	{
		StringBuilder builder = new StringBuilder(bookingType.getName());
		if (builder.length() > 0)
			builder = builder.append(" ");
		builder = builder.append(this.nf.format(bookingType.getPrice()));
		return builder.toString();
	}

	public String formatBookingTypes(final Course course)
	{
		StringBuffer builder = new StringBuffer("");
		Collection<BookingType> bookingTypes = course.getBookingTypes();
		for (BookingType bookingType : bookingTypes)
		{
			if (builder.length() > 0)
				builder.append("\n");
			builder = builder.append(bookingType);
		}
		return bookingTypes.toString();
	}

	public String formatComboEntry(final BookingType bookingType)
	{
		String code = bookingType.getCode();
		String name = bookingType.getName();

		StringBuffer text = new StringBuffer("");

		if (code.length() > 0)
		{
			text = text.append(code);
			if (name.length() > 0)
				text = text.append(" - ");
		}
		if (name.length() > 0)
			text.append(name);

		if (text.length() > 0)
			text.append(", ");

		text.append(NumberFormat.getCurrencyInstance().format(bookingType.getPrice()));
		return text.toString();
	}

	public String formatComboEntry(final CompensationType compensationType)
	{
		String code = compensationType.getCode();
		String name = compensationType.getName();

		StringBuffer text = new StringBuffer("");

		if (code.length() > 0)
		{
			text = text.append(code);
			if (name.length() > 0)
				text = text.append(" - ");
		}

		if (name.length() > 0)
			text.append(name);

		return text.toString();
	}

	public String formatComboEntry(final Course course)
	{
		String code = course.getCode();
		String title = course.getTitle();

		StringBuffer text = new StringBuffer("");

		if (code.length() > 0)
		{
			text = text.append(code);
			if (title.length() > 0)
				text = text.append(" - ");
		}

		if (title.length() > 0)
			text.append(title);

		return text.toString();
	}

	public String formatComboEntry(final CourseDetail courseDetail)
	{
		String start = "";
		if (courseDetail.getStart() != null)
		{
			start = this.sdf.format(courseDetail.getStart().getTime());
		}

		String end = "";
		if (courseDetail.getEnd() != null)
		{
			end = this.sdf.format(courseDetail.getEnd().getTime());
		}

		return start + " - " + end + (courseDetail.getLocation().isEmpty() ? "" : " " + courseDetail.getLocation());
	}

	public String formatComboEntry(final CourseGuide courseGuide)
	{
		String person = PersonFormatter.getInstance().formatLastnameFirstname(
				courseGuide.getGuide().getLink().getPerson());
		String type = courseGuide.getGuideType().getName();
		return type + " - " + person;
	}

	public String formatComboEntry(final GuideType guideType)
	{
		String code = guideType.getCode();
		String name = guideType.getName();

		StringBuffer text = new StringBuffer("");

		if (code.length() > 0)
		{
			text = text.append(code);
			if (name.length() > 0)
				text = text.append(" - ");
		}

		if (name.length() > 0)
			text.append(name);

		return text.toString();
	}

	public String formatCourseGuides(final Course course)
	{
		StringBuffer guides = new StringBuffer("");
		Collection<CourseGuide> courseGuides = course.getCourseGuides();
		if (courseGuides.isEmpty())
			return "";

		for (CourseGuide courseGuide : courseGuides)
		{
			if (guides.length() > 0)
				guides = guides.append("\n");
			guides = guides.append(courseGuide.getGuideType().getName() + ": ");
			guides = guides.append(courseGuide.getGuide().getLink().getPerson().getLastname());
			guides = guides.append(" ");
			guides = guides.append(courseGuide.getGuide().getLink().getPerson().getFirstname());
			if (courseGuide.getGuide().getDescription().length() > 0)
				guides = guides.append(", " + courseGuide.getGuide().getDescription());
			if (courseGuide.getGuide().getPhone().length() > 0)
				guides = guides.append(", " + courseGuide.getGuide().getDescription());
		}
		return guides.toString();
	}

	public String formatFullDate(final Course course)
	{
		StringBuffer dates = new StringBuffer("");
		SimpleDateFormat fdf = new SimpleDateFormat("EEEE, dd.MMMM.yyyy HH:mm");
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd.MMMM.yyyy");
		SimpleDateFormat hdf = new SimpleDateFormat("HH:mm");
		Collection<CourseDetail> details = course.getCourseDetails();
		if (details.isEmpty())
			return "";

		for (CourseDetail detail : details)
		{
			if (dates.length() > 0)
				dates.append("\n");

			if (detail.getStart().get(Calendar.HOUR_OF_DAY) == 0 && detail.getEnd().get(Calendar.MINUTE) == 0)
				dates = dates.append(sdf.format(detail.getStart().getTime()));
			else
				dates = dates.append(fdf.format(detail.getStart().getTime()));

			if (detail.getStart().get(Calendar.DAY_OF_YEAR) == detail.getEnd().get(Calendar.DAY_OF_YEAR)
					&& detail.getStart().get(Calendar.YEAR) == detail.getEnd().get(Calendar.YEAR))
			{
				if (detail.getEnd().get(Calendar.HOUR_OF_DAY) == 0 && detail.getEnd().get(Calendar.MINUTE) == 0)
					dates = dates.append(" ganzer Tag");
				else
					dates = dates.append(" bis " + hdf.format(detail.getEnd().getTime()));
			}
			else
			{
				if (detail.getEnd().get(Calendar.HOUR_OF_DAY) == 0 && detail.getEnd().get(Calendar.MINUTE) == 0)
					dates = dates.append(" bis " + sdf.format(detail.getEnd().getTime()));
				else
					dates = dates.append(" bis " + fdf.format(detail.getEnd().getTime()));
			}

			if (detail.getLocation().length() > 0)
				dates = dates.append("\nOrt: " + detail.getLocation());

			if (detail.getMeetingPoint().length() > 0)
				dates = dates.append("\nTreffpunkt: " + detail.getMeetingPoint());

			if (detail.getJourney().length() > 0)
				dates = dates.append("\nAnreise: " + detail.getJourney());
		}

		return dates.toString();
	}

	public String formatMediumDate(final Course course)
	{
		StringBuffer dates = new StringBuffer("");
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd.MMMM.yyyy HH:mm");
		CourseDetail[] details = course.getCourseDetails().toArray(new CourseDetail[0]);
		if (details.length == 0)
			return "";

		dates = dates.append(sdf.format(details[0].getStart().getTime()));
		dates = dates.append(" bis ");
		dates = dates.append(sdf.format(details[details.length - 1].getEnd().getTime()));
		return dates.toString();
	}

	public String formatPayingParticipant(final Participant participant)
	{
		return PersonFormatter.getInstance().formatLastnameFirstname(participant.getLink().getPerson());
	}

	public String formatPlaces(final Course course)
	{
		StringBuffer places = new StringBuffer("");
		Collection<CourseDetail> details = course.getCourseDetails();
		for (CourseDetail detail : details)
		{
			if (places.length() > 0)
				places.append("\n");
			String location = detail.getLocation();
			places = places.append(location);
			if (location.length() > 0 && detail.getMeetingPoint().length() > 0)
				places = places.append(", ");
			places = places.append(detail.getMeetingPoint());
		}
		return places.toString();
	}

	public String formatShortDate(final Course course)
	{
		StringBuffer dates = new StringBuffer("");
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat mdf = new SimpleDateFormat("dd.MM.");
		SimpleDateFormat ddf = new SimpleDateFormat("dd.");
		CourseDetail[] details = course.getCourseDetails().toArray(new CourseDetail[0]);
		if (details.length == 0)
			return "";

		for (int i = 0; i < details.length; i++)
		{
			if (dates.length() > 0)
				dates = dates.append("/");

			if (details[i].getStart().get(Calendar.YEAR) == details[i].getEnd().get(Calendar.YEAR))
			{
				if (details[i].getStart().get(Calendar.MONTH) == details[i].getEnd().get(Calendar.MONTH))
				{
					if (details[i].getStart().get(Calendar.DATE) == details[i].getEnd().get(Calendar.DATE))
						dates = dates.append(sdf.format(details[i].getStart()));
					else
						dates = dates.append(ddf.format(details[i].getStart() + "-" + sdf.format(details[i].getEnd())));
				}
				else
				{
					dates = dates.append(mdf.format(details[i].getStart()) + "-" + sdf.format(details[i].getEnd()));
				}
			}
			else
			{
				dates = dates.append(sdf.format(details[i].getStart()) + "-" + sdf.format(details[i].getEnd()));
			}
		}
		return dates.toString();
	}

	public static CourseFormatter getInstance()
	{
		if (CourseFormatter.formatter == null)
			CourseFormatter.formatter = new CourseFormatter();

		return CourseFormatter.formatter;
	}

	// public File getInvitationFile(Course course)
	// {
	// IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// IProject project = workspace.getRoot().getProject("Vorlagen");
	// try
	// {
	// if (!project.exists())
	// project.create(null);
	// }
	// catch (CoreException e)
	// {
	//
	// }
	//
	// IFile file = project.getFile(course.getCode() + "_Einladung.odt");
	// return new File(file.getLocationURI());
	// }
	//
	// public File getParticipantListFile(Course course)
	// {
	// IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// IProject project = workspace.getRoot().getProject("Vorlagen");
	// try
	// {
	// if (!project.exists())
	// project.create(null);
	// }
	// catch (CoreException e)
	// {
	//
	// }
	//
	// IFile file = project.getFile(course.getCode() + "_Teilnahmeliste.xls");
	// return new File(file.getLocationURI());
	// }
}
