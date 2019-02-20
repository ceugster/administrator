package ch.eugster.events.course.reporting.dialogs;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.course.reporting.BookingListFactory;
import ch.eugster.events.course.reporting.BookingListItem;
import ch.eugster.events.course.reporting.BookingTypeKey;
import ch.eugster.events.course.reporting.preferences.PreferenceConstants;
import ch.eugster.events.documents.maps.CourseMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.DomainMap;
import ch.eugster.events.documents.maps.RubricMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;

public class BookingListDialog extends TitleAreaDialog
{
	private IDialogSettings settings;

	private Map<CourseState, Boolean> selectedStates = new HashMap<CourseState, Boolean>();
	
	private Map<String, BookingTypeKey> bookingTypeKeys;
	
	private final IStructuredSelection ssel;

	private final String message = "Erstellen einer Kursliste mit Buchungsstand.";

	private boolean isPageComplete = false;
	
	public BookingListDialog(final Shell parentShell, IStructuredSelection ssel)
	{
		super(parentShell);
		this.ssel = ssel;
		settings = Activator.getDefault().getDialogSettings().getSection("booking.list.report.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("booking.list.report.dialog");
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

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(3, true));
		group.setText("Auswahl Status");

		for (final CourseState state : CourseState.values())
		{
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
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout());
		group.setText("Zieldokument");

		final Button reportButton = new Button(group, SWT.RADIO);
		final Button spreadsheetButton = new Button(group, SWT.RADIO);

		boolean selected = settings.getBoolean(TargetDocument.REPORT.name());
		reportButton.setText(TargetDocument.REPORT.label());
		reportButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reportButton.setSelection(selected);
		reportButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				settings.put(TargetDocument.REPORT.name(), reportButton.getSelection());
				settings.put(TargetDocument.SPREADSHEET.name(), !reportButton.getSelection());
			}
		});
		selected = settings.getBoolean(TargetDocument.SPREADSHEET.name());
		spreadsheetButton.setText(TargetDocument.SPREADSHEET.label());
		spreadsheetButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spreadsheetButton.setSelection(selected);
		spreadsheetButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				settings.put(TargetDocument.SPREADSHEET.name(), spreadsheetButton.getSelection());
				settings.put(TargetDocument.REPORT.name(), !spreadsheetButton.getSelection());
			}
		});
		
		return parent;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		this.bookingTypeKeys = new HashMap<String, BookingTypeKey>();
		setCurrentUser();
		UIJob job = new UIJob("Generiere Kurseliste...")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				BookingListFactory factory = BookingListFactory.create(User.getCurrent(), ssel, selectedStates, bookingTypeKeys);
				if (factory.size() == 0)
				{
					MessageDialog.openInformation(null, "Keine Kurse vorhanden", "Ihre Auswahl enthält keine Kurse.");
				}
				else
				{
					Arrays.sort(factory.getBookingListItems());
					if (settings.getBoolean(TargetDocument.REPORT.name()))
					{
						printBookingListReport(factory);
					}
					else
					{
						printBookingListSpreadsheet(factory);
					}
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
		super.setTitle("Kursliste generieren");
	}

	private boolean export(final BookingListFactory factory, final Format format, final File file)
	{
		ServiceTracker<ReportService, ReportService> tracker = new ServiceTracker<ReportService, ReportService>(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class, null);
		tracker.open();
		try
		{
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/booking_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.export(url, factory.getBookingListItems(), parameters, format, file);
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

	private boolean preview(final BookingListFactory factory)
	{
		ServiceTracker<ReportService, ReportService> tracker = new ServiceTracker<ReportService, ReportService>(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class, null);
		tracker.open();
		try
		{
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/booking_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.view(url, factory.getBookingListItems(), parameters);
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

	private boolean print(final BookingListFactory factory, final boolean showPrintDialog)
	{
		ServiceTracker<ReportService, ReportService> tracker = new ServiceTracker<ReportService, ReportService>(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class, null);
		tracker.open();
		try
		{
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/Booking_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.print(url, factory.getBookingListItems(), parameters, showPrintDialog);
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

	private boolean printBookingListReport(final BookingListFactory factory)
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
				FileDialog dialog = new FileDialog(this.createShell());
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

	private void printBookingListSpreadsheet(final BookingListFactory factory)
	{
		setCurrentUser();
		final DataMapKey[] keys = getKeys();
		final DataMap<?>[] dataMaps = createDataMaps(factory).toArray(new DataMap<?>[0]);
		Arrays.sort(dataMaps);

		UIJob job = new UIJob("Dokument wird generiert...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				return buildDocument(monitor, keys, dataMaps);
			}
		};
		job.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void done(final IJobChangeEvent event)
			{
				if (!event.getResult().isOK())
				{
					ErrorDialog.openError(BookingListDialog.this.getShell(), "Verarbeitungsfehler",
							"Beim Generieren des Dokuments ist ein Fehler aufgetreten.", event.getResult(), 0);
				}
			}
		});
		job.schedule();
		super.okPressed();
	}
	
	private IStatus buildDocument(IProgressMonitor monitor, final DataMapKey[] keys, final DataMap<?>[] dataMaps)
	{
		IStatus status = Status.CANCEL_STATUS;
		final ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class, null);
		tracker.open();
		try
		{
			Object[] services = tracker.getServices();
			for (Object service : services)
			{
				if (service instanceof DocumentBuilderService)
				{

					DocumentBuilderService builderService = (DocumentBuilderService) service;
					status = builderService.buildDocument(monitor, keys, dataMaps);
				}
			}
		}
		finally
		{
			tracker.close();
		}
		return status;
	}

	private DataMapKey[] getKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(RubricMap.Key.NAME);
		keys.add(DomainMap.Key.NAME);
		keys.add(CourseMap.Key.CODE);
		keys.add(CourseMap.Key.TITLE);
		keys.add(CourseMap.Key.STATE);
		keys.add(CourseMap.Key.SORTABLE_DATE);
		keys.add(CourseMap.Key.DATE_RANGE_WITH_WEEKDAY_CODE);
		keys.add(CourseMap.Key.GUIDE_WITH_PROFESSION);
		keys.add(CourseMap.Key.ALL_LOCATIONS);
		keys.add(CourseMap.Key.TARGET_PUBLIC);
		BookingTypeKey[] bookingTypeKeys = this.bookingTypeKeys.values().toArray(new BookingTypeKey[0]);
		Arrays.sort(bookingTypeKeys);
		for (BookingTypeKey bookingTypeKey : bookingTypeKeys)
		{
			keys.add(bookingTypeKey);
		}
		return keys.toArray(new DataMapKey[0]);
	}

	private Set<DataMap<?>> createDataMaps(final BookingListFactory factory)
	{
		Set<DataMap<?>> maps = new HashSet<DataMap<?>>();
		BookingListItem[] items = factory.getBookingListItems();
		for (BookingListItem item : items)
		{
			CourseMap map = new CourseMap(item.getCourse());
			Set<String> bookingTypeKeys = item.getBookingTypeCounts().keySet();
			for (String key : bookingTypeKeys)
			{
				map.setProperty(key, item.getBookingTypeCounts().get(key).toString());
			}
			maps.add(map);
		}
		return maps;
	}
	
	private enum TargetDocument
	{
		REPORT, SPREADSHEET;
		
		public String label()
		{
			switch (this)
			{
			case REPORT:
			{
				return "Bericht";
			}
			case SPREADSHEET:
			{
				return "Tabelle";
			}
			default:
			{
				throw new RuntimeException("Invalid Selection for TargetDocument");
			}
			}
		}
	}
}
