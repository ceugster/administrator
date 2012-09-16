package ch.eugster.events.course.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.ActionFactory;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.helpers.ClipboardHelper;

public class CourseCopyAction extends Action implements IAction
{
	private StructuredViewer viewer;

	public CourseCopyAction(StructuredViewer viewer, String label, int accelerator)
	{
		super(label);
		this.viewer = viewer;
		this.setAccelerator(accelerator);
		this.setActionDefinitionId(ActionFactory.COPY.getCommandId());
		this.setId(ActionFactory.COPY.getId());
	}

	@Override
	public void run()
	{
		if (this.viewer != null)
		{
			if (this.viewer.getSelection() instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) this.viewer.getSelection();
				if (ssel.size() == 1)
				{
					CourseTransfer transfer = CourseTransfer.getTransfer();

					if (ssel.getFirstElement() instanceof Season)
					{
						Season[] seasons = new Season[]
						{ (Season) ssel.getFirstElement() };
						transfer.setData(DND.DROP_COPY, seasons);
						ClipboardHelper.getClipboard().setContents(seasons, new Transfer[]
						{ transfer });
					}
					else if (ssel.getFirstElement() instanceof Course)
					{
						Course[] courses = new Course[]
						{ (Course) ssel.getFirstElement() };
						transfer.setData(DND.DROP_COPY, courses);
						ClipboardHelper.getClipboard().setContents(courses, new Transfer[]
						{ transfer });
					}
					else if (ssel.getFirstElement() instanceof Booking)
					{
						Booking[] bookings = new Booking[]
						{ (Booking) ssel.getFirstElement() };
						transfer.setData(DND.DROP_COPY, bookings);
						ClipboardHelper.getClipboard().setContents(bookings, new Transfer[]
						{ transfer });
					}
					else if (ssel.getFirstElement() instanceof Participant)
					{
						Booking[] bookings = new Booking[]
						{ ((Participant) ssel.getFirstElement()).getBooking() };
						transfer.setData(DND.DROP_COPY, bookings);
						ClipboardHelper.getClipboard().setContents(bookings, new Transfer[]
						{ transfer });
					}
				}
			}
		}
	}

	@Override
	public boolean isEnabled()
	{
		if (this.viewer != null)
		{
			if (this.viewer.getSelection() instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) this.viewer.getSelection();
				if (ssel.size() == 1)
				{
					return ssel.getFirstElement() instanceof Season || ssel.getFirstElement() instanceof Course
							|| ssel.getFirstElement() instanceof Booking
							|| ssel.getFirstElement() instanceof Participant;
				}
			}
		}
		return false;
	}

}
