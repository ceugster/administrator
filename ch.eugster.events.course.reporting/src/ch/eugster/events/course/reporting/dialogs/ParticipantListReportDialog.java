package ch.eugster.events.course.reporting.dialogs;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.course.reporting.ParticipantListFactory;
import ch.eugster.events.course.reporting.preferences.PreferenceConstants;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;

public class ParticipantListReportDialog extends TitleAreaDialog
{
	private IDialogSettings settings;

	private Map<IBookingState, Boolean> selectedStates = new HashMap<IBookingState, Boolean>();

	private Map<IBookingState, Integer> bookingStates = new HashMap<IBookingState, Integer>();

	private final Course course;

	private final String message = "Erstellen einer Adressliste des selektierten Kurses.";

	private boolean isPageComplete = false;

	public ParticipantListReportDialog(final Shell parentShell, Course course)
	{
		super(parentShell);
		this.course = course;
		settings = Activator.getDefault().getDialogSettings().getSection("participant.list.report.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("participant.list.report.dialog");
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Generieren", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		Map<CourseState, CourseState> states = new HashMap<CourseState, CourseState>();
		if (states.get(course.getState()) == null)
		{
			states.put(course.getState(), course.getState());
		}

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(3, true));
		group.setText("Auswahl Status");

		int max = Math.max(BookingForthcomingState.values().length, BookingDoneState.values().length);
		max = Math.max(max, BookingAnnulatedState.values().length);
		for (int i = 0; i < max; i++)
		{
			if (i < BookingForthcomingState.values().length)
			{
				final IBookingState state = BookingForthcomingState.values()[i];
				boolean selected = settings.getBoolean(state.name());
				selectedStates.put(state, selected);
				final Button button = new Button(group, SWT.CHECK);
				button.setText(state.toString());
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
						selectedStates.put(state, button.getSelection());
						settings.put(state.name(), button.getSelection());
					}
				});
			}
			else
			{
				Label label = new Label(group, SWT.None);
				label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
			if (i < BookingDoneState.values().length)
			{
				final IBookingState state = BookingDoneState.values()[i];
				boolean selected = settings.getBoolean(state.name());
				selectedStates.put(state, selected);
				final Button button = new Button(group, SWT.CHECK);
				button.setText(state.toString());
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
						selectedStates.put(state, button.getSelection());
						settings.put(state.name(), button.getSelection());
					}
				});
			}
			else
			{
				Label label = new Label(group, SWT.None);
				label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
			if (i < BookingAnnulatedState.values().length)
			{
				final IBookingState state = BookingAnnulatedState.values()[i];
				boolean selected = settings.getBoolean(state.name());
				selectedStates.put(state, selected);
				final Button button = new Button(group, SWT.CHECK);
				button.setText(state.toString());
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
						selectedStates.put(state, button.getSelection());
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

		return parent;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	private Map<IBookingState, Integer> getBookingStates()
	{
		for (Booking booking : course.getBookings())
		{
			IBookingState bookingState = booking.getBookingState(booking.getCourse().getState());
			Boolean state = selectedStates.get(bookingState);
			if (state.booleanValue())
			{
				Integer count = bookingStates.get(bookingState);
				count = new Integer((count == null ? 0 : count) + booking.getParticipantCount());
				bookingStates.put(bookingState, count);
			}
		}
		return bookingStates;
	}

	@Override
	protected void okPressed()
	{
		setCurrentUser();
		this.bookingStates = getBookingStates();
		UIJob job = new UIJob("Generiere Teilnahmeliste...")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				ParticipantListFactory factory = ParticipantListFactory.create(course, bookingStates);
				if (factory.size() == 0)
				{
					MessageDialog.openInformation(null, "Keine Buchungen vorhanden",
							"Für den gewählten Kurs wurden noch keine Buchungen erfasst.");
				}
				else
				{
					Collections.sort(factory.getParticipants());
					printParticipantListReport(factory);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();

		super.okPressed();
	}

	private void setCurrentUser()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				UserQuery query = (UserQuery) service.getQuery(User.class);
				User.setCurrent(query.merge(User.getCurrent()));
			}
		}
		finally
		{
			tracker.close();
		}
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

	public void setTitle()
	{
		super.setTitle("Adressliste generieren");
	}

	private boolean export(final ParticipantListFactory factory, final Format format, final File file)
	{
		ServiceTracker<ReportService, ReportService> tracker = new ServiceTracker<ReportService, ReportService>(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class, null);
		tracker.open();
		try
		{
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/participant_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.export(url, factory.getParticipantsAsArray(), parameters, format, file);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			tracker.close();
		}
		return false;
	}

	private boolean preview(final ParticipantListFactory factory)
	{
		ServiceTracker<ReportService, ReportService> tracker = new ServiceTracker<ReportService, ReportService>(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class, null);
		tracker.open();
		try
		{
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/participant_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.view(url, factory.getParticipantsAsArray(), parameters);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			tracker.close();
		}
		return false;
	}

	private boolean print(final ParticipantListFactory factory, final boolean showPrintDialog)
	{
		ServiceTracker<ReportService, ReportService> tracker = new ServiceTracker<ReportService, ReportService>(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class, null);
		tracker.open();
		try
		{
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/participant_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.print(url, factory.getParticipantsAsArray(), parameters, showPrintDialog);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			tracker.close();
		}
		return false;
	}

	private boolean printParticipantListReport(final ParticipantListFactory factory)
	{
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		int dest = prefs.getInt(PreferenceConstants.P_DESTINATION, 0);
		Destination destination = Destination.values()[dest];
		destination = Destination.PREVIEW;
		switch (destination)
		{
			case PREVIEW:
			{
				return preview(factory);
			}
			case PRINTER:
			{
				boolean useStandardPrinter = prefs.getBoolean(PreferenceConstants.P_USE_STANDARD_PRINTER, false);
				return print(factory, !useStandardPrinter);
			}
			case EXPORT:
			{
				String dir = prefs.get(PreferenceConstants.P_DEFAULT_EXPORT_FILE_DIRECTORY,
						System.getProperty("user.home"));
				Format format = Format.values()[prefs.getInt(PreferenceConstants.P_DEFAULT_FILE_FORMAT,
						Format.PDF.ordinal())];
				FileDialog dialog = new FileDialog(null);
				dialog.setFilterExtensions(Format.extensions());
				dialog.setFilterIndex(format.ordinal());
				dialog.setFilterPath(dir);
				dialog.setText("Dateiname");
				dialog.setFileName("Teilnehmerliste");
				String path = dialog.open();
				if (path == null)
				{
					return false;
				}
				int index = dialog.getFilterIndex();
				if (index > -1)
				{
					format = Format.values()[index];
				}
				if (!path.endsWith(format.extension()))
				{
					path = path + format.extension();
				}
				return export(factory, format, new File(path));
			}
		}
		return false;
	}

}
