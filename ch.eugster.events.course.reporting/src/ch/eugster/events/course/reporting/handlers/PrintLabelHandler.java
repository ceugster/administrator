package ch.eugster.events.course.reporting.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.entries.LabelFactory;
import ch.eugster.events.ui.helpers.EmailHelper;

public class PrintLabelHandler extends AbstractHandler implements IHandler
{
	private int buildLabelList(final LabelFactory factory, final IStructuredSelection ssel)
	{
		Iterator<?> iterator = ssel.iterator();
		while (iterator.hasNext())
		{
			Object element = iterator.next();
			if (element instanceof Season)
			{
				Season season = (Season) element;
				this.extract(factory, season);
			}
			else if (element instanceof Course)
			{
				Course course = (Course) element;
				this.extract(factory, course);
			}
			// else if (element instanceof AddressGroupLink)
			// {
			// this.extract(((AddressGroupLink)
			// element).getChild());
			// }
			else if (element instanceof Booking)
			{
				Booking booking = (Booking) element;
				this.extract(factory, booking);
			}
		}
		return factory.size();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (EmailHelper.getInstance().isEmailSupported())
		{
			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ReportService.class.getName(), null);
				tracker.open();
				ReportService service = (ReportService) tracker.getService();
				if (service != null)
				{
					LabelFactory factory = new LabelFactory();
					EvaluationContext context = (EvaluationContext) event.getApplicationContext();
					ISelection sel = (ISelection) context.getParent().getVariable("selection");
					IStructuredSelection ssel = (IStructuredSelection) sel;
					if (buildLabelList(factory, ssel) > 0)
					{
						Destination[] destinations = new Destination[] { Destination.PREVIEW, Destination.PRINTER };
						service.processLabels(factory.getEntries(), new HashMap<String, Object>(), destinations);
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private void extract(final LabelFactory factory, final Booking booking)
	{
		if (booking.getParticipant() != null)
		{
			Participant participant = booking.getParticipant();
			factory.addEntry(participant.getLink());
		}
	}

	private void extract(final LabelFactory factory, final Course course)
	{
		if (!course.isDeleted())
		{
			Collection<Booking> bookings = course.getBookings();
			for (Booking booking : bookings)
			{
				this.extract(factory, booking);
			}
			Collection<CourseGuide> guides = course.getCourseGuides();
			for (CourseGuide guide : guides)
			{
				this.extract(factory, guide);
			}
			// for (AddressGroupLink link : addressGroup.getChildren())
			// {
			// if (!link.isDeleted() && !link.getChild().isDeleted())
			// {
			// extract(link.getChild());
			// }
			// }
		}
	}

	private void extract(final LabelFactory factory, final CourseGuide courseGuide)
	{
		if (!courseGuide.isDeleted())
		{
			LinkPersonAddress link = courseGuide.getGuide().getLink();
			factory.addEntry(link);
		}
	}

	private void extract(final LabelFactory factory, final Season season)
	{
		if (!season.isDeleted())
		{
			Collection<Course> courses = season.getCourses();
			for (Course course : courses)
			{
				this.extract(factory, course);
			}
		}
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		Object selection = context.getVariable("selection");
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.getFirstElement() instanceof Season)
			{
				enabled = true;
			}
			else if (ssel.getFirstElement() instanceof Course)
			{
				enabled = true;
			}
			else if (ssel.getFirstElement() instanceof Booking)
			{
				enabled = true;
			}
		}
		setBaseEnabled(enabled);
	}
}
