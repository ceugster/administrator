package ch.eugster.events.course.reporting.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.documents.maps.BookingMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.UserProperty;
import ch.eugster.events.persistence.queries.CourseQuery;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseInvitationDialog extends TitleAreaDialog
{
	private Text documentPath;

	private Button documentSelector;

	private Button updateCourse;
	
	private final StructuredSelection selection;

	private UserProperty userPropertyTemplatePath;

	private final String message = "Wählen Sie das Dokument, das als Vorlage verwendet werden soll.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service für die Verarbeitung des Dokuments verfügbar.";

	private static final String MSG_TITLE_NO_COURSES = "Keine Buchungen vorhanden";

	private static final String OK_BUTTON_TEXT = "Generieren";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Vorlage Kurseinladung";

	private static final String DIALOG_MSG = "Die Einladungen werden nur für angemeldete Personen erstellt.";
	
	private boolean isPageComplete = false;

	public CourseInvitationDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private void buildDocument(final DataMap[] dataMaps)
	{
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle()
							.getBundleContext(), DocumentBuilderService.class, null);
					tracker.open();
					try
					{
						ServiceReference<DocumentBuilderService>[] references = tracker.getServiceReferences();
						if (references != null)
						{
							try
							{
								monitor.beginTask("Dokumente werden erstellt...", references.length);
								for (ServiceReference<DocumentBuilderService> reference : references)
								{
									DocumentBuilderService service = (DocumentBuilderService) tracker
											.getService(reference);
									DocumentBuilderService builderService = service;
									IStatus status = builderService.buildDocument(new SubProgressMonitor(monitor,
											dataMaps.length), new File(userPropertyTemplatePath.getValue()), dataMaps);
									if (status.isOK() || status.getSeverity() == IStatus.ERROR)
									{
										break;
									}
									monitor.worked(1);
								}
							}
							finally
							{
								monitor.done();
							}
						}
						else
						{
							MessageDialog.openWarning(getShell(), "Service nicht aktiv", MSG_NO_SERVICE_AVAILABLE);
						}
					}
					finally
					{
						tracker.close();
					}
				}
			});
			if (updateCourse != null && updateCourse.getSelection())
			{
				ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
				connectionServiceTracker.open();
				try
				{
					ConnectionService connectionService = (ConnectionService) connectionServiceTracker.getService();
					if (connectionService != null)
					{
						CourseQuery query = (CourseQuery) connectionService.getQuery(Course.class);
						@SuppressWarnings("rawtypes")
						Iterator iterator = this.selection.iterator();
						while (iterator.hasNext())
						{
							Object object = iterator.next();
							if (object instanceof Course)
							{
								Course course = (Course) object;
								course.setInvitationDoneDate(Calendar.getInstance());
								course = query.merge(course);
							}
						}
					}
				}
				finally
				{
					connectionServiceTracker.close();
				}
			}
		}
		catch (InvocationTargetException e)
		{
			MessageDialog.openWarning(getShell(), "Fehler", "Ein Fehler ist aufgetreten.\n(" + e.getLocalizedMessage()
					+ ")");
		}
		catch (InterruptedException e)
		{
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, OK_BUTTON_TEXT, true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, CANCEL_BUTTON_TEXT, false);
		File file = new File(documentPath.getText());
		this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
	}

	private List<DataMap> createDataMaps()
	{
		List<DataMap> dataMaps = new ArrayList<DataMap>();
		Object[] elements = selection.toArray();
		for (int i = elements.length; i > 0; i--)
		{
			Object element = elements[i - 1];
			if (element instanceof Course)
			{
				Course course = (Course) element;
				List<Booking> bookings = course.getBookings();
				for (Booking booking : bookings)
				{
					if (booking.getState().equals(BookingForthcomingState.BOOKED))
					{
						dataMaps.add(new BookingMap(booking, true));
					}
				}
				List<CourseGuide> courseGuides = course.getCourseGuides();
				for (CourseGuide courseGuide : courseGuides)
				{
					LinkPersonAddress link = courseGuide.getGuide().getLink();
					Booking booking = Booking.newInstance(courseGuide.getCourse());
					Participant participant = Participant.newInstance(link, booking);
					booking.setParticipant(participant);
					dataMaps.add(new BookingMap(booking, true));
				}
			}
			else if (element instanceof Booking)
			{
				Booking booking = (Booking) element;
				dataMaps.add(new BookingMap(booking, true));
			}
		}
		return dataMaps;
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle(DIALOG_TITLE);
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Vorlage");

		File file = null;
		if (User.getCurrent() != null)
		{
			this.userPropertyTemplatePath = User.getCurrent().getProperty(
					UserProperty.Property.COURSE_INVITATION_TEMPLATE_PATH.key());
			if (this.userPropertyTemplatePath == null)
			{
				this.userPropertyTemplatePath = UserProperty.newInstance(User.getCurrent());
				this.userPropertyTemplatePath.setKey(UserProperty.Property.COURSE_INVITATION_TEMPLATE_PATH.key());
				this.userPropertyTemplatePath.setValue(System.getProperty("user.home"));
			}
			file = new File(this.userPropertyTemplatePath.getValue());
		}
		documentPath = new Text(composite, SWT.BORDER | SWT.SINGLE);
		documentPath.setText(file.getAbsolutePath());
		documentPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		documentPath.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				File file = new File(documentPath.getText());
				if (file.exists())
				{
					userPropertyTemplatePath.setValue(file.getAbsolutePath());
				}
			}
		});

		documentSelector = new Button(composite, SWT.PUSH);
		documentSelector.setText("...");
		documentSelector.setLayoutData(new GridData());
		documentSelector.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				File file = new File(CourseInvitationDialog.this.documentPath.getText());
				while (!file.exists())
				{
					file = file.getParentFile();
				}
				FileDialog dialog = new FileDialog(CourseInvitationDialog.this.getShell());
				dialog.setFilterPath(file.getAbsolutePath());
				dialog.setFilterExtensions(new String[] { "*.odt" });
				dialog.setText(DIALOG_TITLE);
				String path = dialog.open();
				if (path != null)
				{
					CourseInvitationDialog.this.documentPath.setText(path);

				}
				file = new File(CourseInvitationDialog.this.documentPath.getText());
				if (file.exists())
				{
					userPropertyTemplatePath.setValue(file.getAbsolutePath());
				}
				CourseInvitationDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
			}
		});

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		
		label = new Label(composite, SWT.None);
		label.setText(DIALOG_MSG);
		label.setLayoutData(gridData);

		if (!noDirtyCourses())
		{
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			
			updateCourse = new Button(composite, SWT.CHECK);
			updateCourse.setLayoutData(gridData);
			updateCourse.setText("Kurs aktualisieren (Datum 'Einladung verschickt' setzen)");
		}
		return parent;
	}

	private boolean noDirtyCourses()
	{
		@SuppressWarnings("rawtypes")
		Iterator iterator = this.selection.iterator();
		while (iterator.hasNext())
		{
			Object object = iterator.next();
			if (object instanceof Course)
			{
				IEditorReference[] references = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
				for (IEditorReference reference : references)
				{
					IEditorPart part = reference.getEditor(false);
					if (part != null && part.getEditorInput().getAdapter(Course.class) != null)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setUserPath();
		DataMap[] dataMaps = createDataMaps().toArray(new DataMap[0]);
		if (dataMaps.length == 0)
		{
			MessageDialog.openConfirm(this.getShell(), MSG_TITLE_NO_COURSES, MSG_TITLE_NO_COURSES);
		}
		else
		{
			buildDocument(dataMaps);
		}
		super.okPressed();
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

	private void setUserPath()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			Object service = tracker.getService();
			if (service instanceof ConnectionService)
			{
				this.userPropertyTemplatePath.setUser(User.getCurrent());
				ConnectionService connectionService = (ConnectionService) service;
				User.getCurrent().setProperty(this.userPropertyTemplatePath);
				UserQuery query = (UserQuery) connectionService.getQuery(User.class);
				User.setCurrent(query.merge(User.getCurrent()));
			}
		}
		finally
		{
			tracker.close();
		}
	}
}
