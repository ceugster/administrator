package ch.eugster.events.course.reporting.dialogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.entries.LabelFactory;

public class PrintLabelDialog extends TitleAreaDialog
{
	private final IStructuredSelection selection;

	private Map<IBookingState, Boolean> bookingStates = new HashMap<IBookingState, Boolean>();

	private IDialogSettings settings;

	private final String message = "Wählen Sie die Optionen für den Etikettendruck.";

	private static final String MSG_NO_BOOKINGS = "Es sind keine Buchungen zu verarbeiten.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service für die Verarbeitung des Dokuments verfügbar.";

	private static final String MSG_TITLE_NO_BOOKINGS = "Keine Buchungen vorhanden";

	private static final String OK_BUTTON_TEXT = "Drucken";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Etiketten drucken";

	private boolean isPageComplete = false;

	/**
	 * @param parentShell
	 * @param parent
	 *            <code>parent</code> must be of type
	 *            ch.eugster.events.data.objects.Customer
	 * @param addressGroup
	 *            Falls eine neue Adressgruppe erfasst wird, muss diese bereit
	 *            vor der Übergabe an den Konstruktor von
	 *            <code>AddressGroupDialog</code> instantiiert sein und der
	 *            Parent <code>Domain</code> muss - falls ein solcher gesetzt
	 *            werden soll, ebenfalls dem Konstruktur von
	 *            <code>AddressGroup</code> übergeben worden sein.
	 * 
	 */
	public PrintLabelDialog(final Shell parentShell, final IStructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
		settings = Activator.getDefault().getDialogSettings().getSection("print.label.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("print.label.dialog");
		}
	}

	private void buildDocument()
	{
		UIJob job = new UIJob("Etiketten werden vorbereitet...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				IStatus status = Status.OK_STATUS;

				LabelFactory factory = new LabelFactory();
				Iterator<?> iterator = selection.iterator();
				while (iterator.hasNext())
				{
					Object element = iterator.next();
					if (element instanceof Season)
					{
						Season season = (Season) element;
						extract(factory, season);
					}
					else if (element instanceof Course)
					{
						Course course = (Course) element;
						extract(factory, course);
					}
					// else if (element instanceof AddressGroupLink)
					// {
					// this.extract(((AddressGroupLink)
					// element).getChild());
					// }
					else if (element instanceof Booking)
					{
						Booking booking = (Booking) element;
						extract(factory, booking);
					}
				}

				if (factory.size() == 0)
				{
					status = new Status(IStatus.CANCEL, Activator.getDefault().getBundle().getSymbolicName(),
							MSG_NO_BOOKINGS);
				}
				else
				{
					ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
							ReportService.class.getName(), null);
					try
					{
						tracker.open();
						ReportService service = (ReportService) tracker.getService();
						if (service == null)
						{
							status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
									MSG_NO_SERVICE_AVAILABLE);
						}
						else
						{
							service.processLabels(factory.getEntries(), new HashMap<String, Object>(),
									new Destination[] { Destination.PREVIEW, Destination.PRINTER });
							status = Status.OK_STATUS;
						}
					}
					finally
					{
						tracker.close();
					}
				}
				return status;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void done(final IJobChangeEvent event)
			{
				IStatus status = event.getResult();
				if (!status.isOK())
				{
					if (status.getSeverity() == IStatus.CANCEL)
					{
						MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), MSG_TITLE_NO_BOOKINGS, null, status.getMessage(),
								MessageDialog.INFORMATION, new String[] { "OK" }, 0);
						dialog.open();
					}
					else
					{
						ErrorDialog dialog = new ErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), MSG_TITLE_NO_BOOKINGS, status.getMessage(), status, 0);
						dialog.open();
					}
				}
			}
		});
		job.setUser(true);
		job.schedule();
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, OK_BUTTON_TEXT, true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, CANCEL_BUTTON_TEXT, false);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle(DIALOG_TITLE);
		this.setMessage();

		Map<CourseState, CourseState> states = new HashMap<CourseState, CourseState>();
		Object[] objects = selection.toArray();
		for (Object object : objects)
		{
			if (object instanceof Season)
			{
				Season season = (Season) object;
				Collection<Course> courses = season.getCourses();
				for (Course course : courses)
				{
					if (states.get(course.getState()) == null)
					{
						states.put(course.getState(), course.getState());
					}
				}
			}
			else if (object instanceof Course)
			{
				Course course = (Course) object;
				if (states.get(course.getState()) == null)
				{
					states.put(course.getState(), course.getState());
				}
			}
			else if (object instanceof Booking)
			{
				Booking booking = (Booking) object;
				if (states.get(booking.getCourse().getState()) == null)
				{
					states.put(booking.getCourse().getState(), booking.getCourse().getState());
				}
			}
		}

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		final Button guides = new Button(composite, SWT.CHECK);
		guides.setText("Kursleitung");
		guides.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		guides.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				settings.put("guides", guides.getSelection());
			}
		});
		guides.setSelection(settings.getBoolean("guides"));

		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout(states.size(), true));
		group.setText("Auswahl Status");

		int max = 0;
		for (CourseState state : states.values())
		{
			max = Math.max(max, state.getBookingStates().length);
		}
		for (int i = 0; i < max; i++)
		{
			if (states.get(CourseState.FORTHCOMING) != null)
			{
				if (i < BookingForthcomingState.values().length)
				{
					final IBookingState state = BookingForthcomingState.values()[i];
					boolean selected = settings.getBoolean(state.name());
					bookingStates.put(state, selected);
					final Button button = new Button(group, SWT.CHECK);
					button.setText(BookingForthcomingState.values()[i].toString());
					button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					button.setSelection(selected);
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
							bookingStates.put(state, button.getSelection());
							settings.put(state.name(), button.getSelection());
						}
					});
				}
				else
				{
					Label label = new Label(group, SWT.None);
					label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				}
			}
			if (states.get(CourseState.DONE) != null)
			{
				if (i < BookingDoneState.values().length)
				{
					final IBookingState state = BookingDoneState.values()[i];
					boolean selected = settings.getBoolean(state.name());
					bookingStates.put(state, selected);
					final Button button = new Button(group, SWT.CHECK);
					button.setText(BookingDoneState.values()[i].toString());
					button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					button.setSelection(selected);
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
							bookingStates.put(state, button.getSelection());
							settings.put(state.name(), button.getSelection());
						}
					});
				}
				else
				{
					Label label = new Label(group, SWT.None);
					label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				}
			}
			if (states.get(CourseState.ANNULATED) != null)
			{
				if (i < BookingAnnulatedState.values().length)
				{
					final IBookingState state = BookingAnnulatedState.values()[i];
					boolean selected = settings.getBoolean(state.name());
					bookingStates.put(state, selected);
					final Button button = new Button(group, SWT.CHECK);
					button.setText(BookingAnnulatedState.values()[i].toString());
					button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					button.setSelection(selected);
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
							bookingStates.put(state, button.getSelection());
							settings.put(state.name(), button.getSelection());
						}
					});
				}
				else
				{
					Label label = new Label(group, SWT.None);
					label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				}
			}
		}
		return parent;
	}

	private void extract(final LabelFactory factory, final Booking booking)
	{
		if (!booking.isDeleted() && booking.getParticipant() != null)
		{
			Object object = bookingStates.get(booking.getBookingState(booking.getCourse().getState()));
			if (object instanceof Boolean)
			{
				Boolean value = (Boolean) object;
				if (value.booleanValue())
				{
					Participant participant = booking.getParticipant();
					if (!participant.isDeleted() && !participant.getLink().isDeleted())
					{
						factory.addEntry(participant.getLink());
					}
				}
			}
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
			if (settings.getBoolean("guides"))
			{
				Collection<CourseGuide> guides = course.getCourseGuides();
				for (CourseGuide guide : guides)
				{
					this.extract(factory, guide);
				}
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
			if (!link.isDeleted())
			{
				factory.addEntry(link);
			}
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

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		super.okPressed();
		buildDocument();
	}

	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
		this.setPageComplete(false);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		super.setMessage(this.message);
		this.setPageComplete(true);
	}

	public void setPageComplete(final boolean isComplete)
	{
		this.isPageComplete = isComplete;
		if (this.getButton(IDialogConstants.OK_ID) != null)
			this.getButton(IDialogConstants.OK_ID).setEnabled(this.isPageComplete);
	}

}
