package ch.eugster.events.course.reporting.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.documents.maps.BookingMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.UserProperty;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class BookingConfirmationDialog extends TitleAreaDialog
{
	private Text documentPath;

	private Button documentSelector;

	private final StructuredSelection selection;

	private UserProperty userPropertyTemplatePath;

	private final String message = "Wählen Sie das Dokument, das als Vorlage verwendet werden soll.";

	private static final String MSG_NO_BOOKINGS = "Es sind keine Buchungen zu verarbeiten.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service für die Verarbeitung des Dokuments verfügbar.";

	private static final String MSG_TITLE_NO_BOOKINGS = "Keine Buchungen vorhanden";

	private static final String OK_BUTTON_TEXT = "Generieren";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Vorlage Buchungsbestätigung";

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
	public BookingConfirmationDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private void buildDocument()
	{
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					DataMap<?>[] maps = createDataMaps().toArray(new DataMap[0]);
					if (maps.length == 0)
					{
						Job job = new UIJob("") 
						{
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) 
							{
								MessageDialog.openConfirm(getShell(), MSG_TITLE_NO_BOOKINGS, MSG_NO_BOOKINGS);
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
					else
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
												maps.length), new File(userPropertyTemplatePath.getValue()), maps);
										if (status.isOK())
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
								Job job = new UIJob("") 
								{
									@Override
									public IStatus runInUIThread(IProgressMonitor monitor) 
									{
										MessageDialog.openWarning(getShell(), "Service nicht aktiv", MSG_NO_SERVICE_AVAILABLE);
										return Status.OK_STATUS;
									}
								};
								job.schedule();
							}
						}
						finally
						{
							tracker.close();
						}
					}
				}
			});
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

	private void createDataMap(final Booking booking, final Map<Long, DataMap<?>> dataMaps)
	{
		if (booking.isValid() && booking.getBookingState(CourseState.FORTHCOMING).equals(BookingForthcomingState.BOOKED))
		{
			dataMaps.put(booking.getId(), new BookingMap(booking, true));
		}
	}

	private Collection<DataMap<?>> createDataMaps()
	{
		Map<Long, DataMap<?>> dataMaps = new HashMap<Long, DataMap<?>>();
		Object[] elements = selection.toArray();
		for (Object element : elements)
		{
			if (element instanceof Course)
			{
				Course course = (Course) element;
				createDataMaps(course, dataMaps);
			}
			else if (element instanceof Booking)
			{
				Booking booking = (Booking) element;
				createDataMap(booking, dataMaps);
			}
		}
		return dataMaps.values();
	}

	private void createDataMaps(final Course course, final Map<Long, DataMap<?>> dataMaps)
	{
		if (!course.isDeleted())
		{
			List<Booking> bookings = course.getBookings();
			for (Booking booking : bookings)
			{
				createDataMap(booking, dataMaps);
			}
		}
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
					UserProperty.Property.BOOKING_CONFIRMATION_TEMPLATE_PATH.key());
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
		documentSelector.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				String path = BookingConfirmationDialog.this.documentPath.getText();
				FileDialog dialog = new FileDialog(BookingConfirmationDialog.this.getShell());
				dialog.setFilterPath(path);
				dialog.setFilterExtensions(new String[] { "*.odt" });
				dialog.setText(DIALOG_TITLE);
				path = dialog.open();
				if (path != null)
				{
					BookingConfirmationDialog.this.documentPath.setText(path);

				}
				File file = new File(BookingConfirmationDialog.this.documentPath.getText());
				if (file.exists())
				{
					userPropertyTemplatePath.setValue(file.getAbsolutePath());
				}
				BookingConfirmationDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
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
		setUserPath();
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
