package ch.eugster.events.course.handlers;

import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.dialogs.CourseCopyDialog;
import ch.eugster.events.course.views.CourseView;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Compensation;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.SeasonQuery;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;
import ch.eugster.events.ui.helpers.ClipboardHelper;

public class CoursePasteHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getVariable("activePart") instanceof CourseView)
			{
				CourseView view = (CourseView) context.getVariable("activePart");
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Season)
				{
					Season season = (Season) ssel.getFirstElement();
					Object[] objects = (Object[]) ClipboardHelper.getClipboard().getContents(CourseTransfer.getTransfer());
					if (objects.length > 0)
					{
						Shell shell = (Shell) context.getVariable("activeShell");
						CourseCopyDialog dialog = new CourseCopyDialog(shell, ssel);
						if (dialog.open() == 0)
						{
							int inserted = 0;
							for (Object object : objects)
							{
								if (object instanceof Course)
								{
									Course course = (Course) object;
									inserted += insertCourse(season, course, CourseTransfer.getTransfer().getOperation());
								}
							}
							if (inserted > 0)
							{
								updateSeason(season);
							}
						}
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private int insertCourse(final Season target, final Course course,
			final int type)
	{
		int inserted = 0;
		if (!course.isDeleted())
		{
			inserted = 1;
			Course newCourse = course.copy(target);
			for (Option option : Option.values())
			{
				option.updateCourse(course, newCourse);
			}
			target.addCourse(newCourse);
			
			if (type == DND.DROP_MOVE)
			{
				course.setDeleted(true);
			}
		}
		return inserted;
	}

	private void updateSeason(Season season)
	{
		if (connectionService != null)
		{
			SeasonQuery query = (SeasonQuery) connectionService.getQuery(Season.class);
			season = query.merge(season);
		}
	}

	public enum Option
	{
		COPY_GUIDES, COPY_DETAILS, COPY_BOOKING_TYPES;
		
		Button button;

		boolean value = false;
		
		public Button getButton(final Composite parent, final IDialogSettings settings)
		{
			button = new Button(parent, SWT.CHECK);
			button.setText(this.getLabel());
			button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			button.setSelection(settings.getBoolean(this.getSettingsKey()));
			button.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(final SelectionEvent e)
				{
					widgetSelected(e);
				}

				@Override
				public void widgetSelected(final SelectionEvent e)
				{
					settings.put(Option.this.getSettingsKey(), ((Button)e.widget).getSelection());
					value = settings.getBoolean(Option.this.getSettingsKey());
				}
			});
			value = settings.getBoolean(Option.this.getSettingsKey());
			return button;
		}
		
		public String getLabel()
		{
			switch (this)
			{
			case COPY_GUIDES:
			{
				return "Kursleitung übernehmen";
			}
			case COPY_DETAILS:
			{
				return "Kursdetails übernehmen";
			}
			case COPY_BOOKING_TYPES:
			{
				return "Buchungsarten übernehmen";
			}
			default:
			{
				throw new IllegalArgumentException();
			}
			}
		}

		public String getSettingsKey()
		{
			switch (this)
			{
			case COPY_GUIDES:
			{
				return "copy.course.guides";
			}
			case COPY_DETAILS:
			{
				return "copy.course.details";
			}
			case COPY_BOOKING_TYPES:
			{
				return "copy.booking.types";
			}
			default:
			{
				throw new IllegalArgumentException();
			}
			}
		}
		
		public boolean getValue()
		{
			return value;
		}
		
		public void updateCourse(Course source, Course target)
		{
			if (this.getValue())
			{
				switch (this)
				{
				case COPY_GUIDES:
				{
					List<CourseGuide> guides = source.getCourseGuides();
					for (CourseGuide guide : guides)
					{
						CourseGuide newGuide = CourseGuide.newInstance(target);
						newGuide.setGuide(guide.getGuide());
						newGuide.setInserted(GregorianCalendar.getInstance());
						newGuide.setDescription(guide.getDescription());
						newGuide.setGuideType(guide.getGuideType());
						newGuide.setNote(guide.getNote());
						newGuide.setPhone(guide.getPhone());
						newGuide.setUser(User.getCurrent());
						List<Compensation> compensations = guide.getCompensations();
						for (Compensation compensation : compensations)
						{
							Compensation newCompensation = Compensation.newInstance(newGuide);
							newCompensation.setAmount(compensation.getAmount());
							newCompensation.setCompensationType(compensation.getCompensationType());
							newCompensation.setUser(User.getCurrent());
							newCompensation.setInserted(GregorianCalendar.getInstance());
							newGuide.addCompensation(newCompensation);
						}
//						guide.getGuide().addCourseGuide(newGuide);
						target.addCourseGuide(newGuide);
					}
					return;
				}
				case COPY_DETAILS:
				{
					List<CourseDetail> details = source.getCourseDetails();
					for (CourseDetail detail : details)
					{
						target.addCourseDetail(detail.copy(target));
					}
					return;
				}
				case COPY_BOOKING_TYPES:
				{
					List<BookingType> bookingTypes = source.getBookingTypes();
					for (BookingType bookingType : bookingTypes)
					{
						BookingType newBookingType = BookingType.newInstance(target);
						newBookingType.setAnnulationCharges(bookingType.getAnnulationCharges());
						newBookingType.setCode(bookingType.getCode());
						newBookingType.setMaxAge(bookingType.getMaxAge());
						newBookingType.setMembership(bookingType.getMembership());
						newBookingType.setName(bookingType.getName());
						newBookingType.setPrice(bookingType.getPrice());
						newBookingType.setUser(User.getCurrent());
						target.addBookingType(newBookingType);
					}
					return;
				}
				default:
				{
					throw new IllegalArgumentException();
				}
				}
			}
		}
		
	}
}
