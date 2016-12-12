package ch.eugster.events.course.reporting.dialogs;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.documents.maps.CourseMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.RubricMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseListDialog extends TitleAreaDialog
{
	private IDialogSettings settings;

	private Map<CourseState, Boolean> selectedStates = new HashMap<CourseState, Boolean>();

	private final IStructuredSelection ssel;

	private final String message = "Erstellen einer Kursliste.";

	private boolean isPageComplete = false;

	public CourseListDialog(final Shell parentShell, IStructuredSelection ssel)
	{
		super(parentShell);
		this.ssel = ssel;
		settings = Activator.getDefault().getDialogSettings().getSection("course.list.spreadsheet.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("course.list.spreadsheet.dialog");
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
		
		return parent;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setCurrentUser();
		UIJob job = new UIJob("Generiere Kurseliste...")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				DataMap<?>[] courseMaps = createDataMaps();
				if (courseMaps.length == 0)
				{
					MessageDialog.openInformation(null, "Keine Kurse vorhanden", "Ihre Auswahl enthält keine Kurse.");
				}
				else
				{
					Arrays.sort(courseMaps);
					printSpreadsheet(courseMaps);
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

	private void printSpreadsheet(final DataMap<?>[] dataMaps)
	{
		setCurrentUser();
		final DataMapKey[] keys = getKeys();

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
					ErrorDialog.openError(CourseListDialog.this.getShell(), "Verarbeitungsfehler",
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
		keys.add(CourseMap.Key.CODE);
		keys.add(CourseMap.Key.TITLE);
		keys.add(CourseMap.Key.SORTABLE_DATE);
		keys.add(CourseMap.Key.DATE_RANGE_WITH_WEEKDAY_CODE);
		keys.add(CourseMap.Key.GUIDE_WITH_PROFESSION);
		keys.add(CourseMap.Key.ALL_LOCATIONS);
		keys.add(CourseMap.Key.TARGET_PUBLIC);
		return keys.toArray(new DataMapKey[0]);
	}

	private DataMap<?>[] createDataMaps()
	{
		Set<Course> courses = new HashSet<Course>();
		Object[] objects = this.ssel.toArray();
		for (Object object : objects)
		{
			if (object instanceof Season)
			{
				Season season = (Season) object;
				for (Course course : season.getCourses())
				{
					addCourse(course, courses);
				}
			}
			else if (object instanceof Course)
			{
				Course course = (Course) object;
				addCourse(course, courses);
			}
		}

		List<DataMap<?>> courseMapList = new ArrayList<DataMap<?>>();
		for (Course course : courses)
		{
			courseMapList.add(new CourseMap(course));
		}
		return courseMapList.toArray(new CourseMap[0]);
	}

	private boolean addCourse(Course course, Set<Course> courses)
	{
		if (!course.isDeleted())
		{
			Boolean state = selectedStates.get(course.getState());
			if (state != null && state.booleanValue())
			{
				return courses.add(course);
			}
		}
		return false;
	}

}
