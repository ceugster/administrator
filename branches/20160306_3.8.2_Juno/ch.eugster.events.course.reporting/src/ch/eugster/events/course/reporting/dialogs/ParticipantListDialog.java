package ch.eugster.events.course.reporting.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.CourseGuideMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.maps.ParticipantMap;
import ch.eugster.events.documents.maps.PersonMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class ParticipantListDialog extends TitleAreaDialog
{
	// private Button ListSelector;

	private final StructuredSelection selection;

	private final String message = "Erstellen einer Adressliste der selektierten Kurse.";

	private boolean isPageComplete = false;

	public ParticipantListDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private void buildDocument(IProgressMonitor monitor, final DataMapKey[] keys, final DataMap<?>[] dataMaps)
	{
		IStatus status = Status.CANCEL_STATUS;
		ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class, null);
		try
		{
			tracker.open();
			ServiceReference<DocumentBuilderService>[] serviceReferences = tracker.getServiceReferences();
			if (serviceReferences != null)
			{
				try
				{
					monitor.beginTask("Dokument wird erstellt...", serviceReferences.length);
					for (ServiceReference<DocumentBuilderService> serviceReference : serviceReferences)
					{
						if (status.isOK())
						{
							monitor.worked(1);
							return;
						}
						DocumentBuilderService builderService = (DocumentBuilderService) tracker.getService(serviceReference);
						status = builderService.buildDocument(new SubProgressMonitor(monitor, dataMaps.length),
								keys, dataMaps);
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
		}
		finally
		{
			tracker.close();
		}
	}

	private void computeBooking(final List<DataMap<?>> map, final Booking booking)
	{
		if (!booking.isDeleted())
		{
			List<Participant> participants = booking.getParticipants();
			for (Participant participant : participants)
			{
				computeParticipant(map, participant);
			}
		}
	}

	private void computeCourseGuide(final List<DataMap<?>> map, final CourseGuide courseGuide)
	{
		if (!courseGuide.isDeleted())
		{
			map.add(new CourseGuideMap(courseGuide, false));
		}
	}

	private void computeSeason(final List<DataMap<?>> map, final Season season)
	{
		if (!season.isDeleted())
		{
			List<Course> courses = season.getCourses();
			for (Course course : courses)
			{
				computeCourse(map, course);
			}
		}
	}

	private void computeCourse(final List<DataMap<?>> map, final Course course)
	{
		if (!course.isDeleted())
		{
			List<Booking> bookings = course.getBookings();
			for (Booking booking : bookings)
			{
				computeBooking(map, booking);
			}
			List<CourseGuide> courseGuides = course.getCourseGuides();
			for (CourseGuide courseGuide : courseGuides)
			{
				computeCourseGuide(map, courseGuide);
			}
		}
	}

	private void computeParticipant(final List<DataMap<?>> map, final Participant participant)
	{
		if (!participant.isDeleted())
		{
			map.add(new ParticipantMap(participant));
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Generieren", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	private List<DataMap<?>> createDataMaps(final StructuredSelection ssel)
	{
		List<DataMap<?>> maps = new ArrayList<DataMap<?>>();
		Object[] elements = ssel.toArray();
		for (Object element : elements)
		{
			if (element instanceof Season)
			{
				Season season = (Season) element;
				computeSeason(maps, season);
			}
			else if (element instanceof Course)
			{
				Course course = (Course) element;
				computeCourse(maps, course);
			}
		}
		return maps;
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		// if
		// (EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]
		// .equals(EditorSelector.MULTI_PAGE_EDITOR))
		// {
		// ListSelector = new Button(composite, SWT.CHECK);
		// ListSelector.setText("Gruppenadressen nur einmal auflisten");
		// ListSelector.setLayoutData(new GridData());
		// }

		return parent;
	}

	private DataMapKey[] getKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(AddressMap.Key.NAME);
		keys.add(AddressMap.Key.PHONE);
		keys.add(AddressMap.Key.ADDRESS);
		keys.add(AddressMap.Key.POB);
		keys.add(AddressMap.Key.COUNTRY);
		keys.add(AddressMap.Key.ZIP);
		keys.add(AddressMap.Key.CITY);
		keys.add(AddressMap.Key.COUNTY);
		keys.add(AddressMap.Key.FAX);
		keys.add(LinkMap.Key.PHONE);
		keys.add(ParticipantMap.Key.BOOKING_TYPE_CODE);
		keys.add(ParticipantMap.Key.BOOKING_TYPE_NAME);
		keys.add(ParticipantMap.Key.ID);
		keys.add(ParticipantMap.Key.SALUTATION);
		keys.add(ParticipantMap.Key.ANOTHER_LINE);
		keys.add(ParticipantMap.Key.POLITE);
		keys.add(ParticipantMap.Key.PRICE);
		keys.add(ParticipantMap.Key.MAILING_ADDRESS);
		keys.add(ParticipantMap.Key.COUNT);
		keys.add(PersonMap.Key.SEX);
		keys.add(PersonMap.Key.FORM);
		keys.add(PersonMap.Key.TITLE);
		keys.add(PersonMap.Key.FIRSTNAME);
		keys.add(PersonMap.Key.LASTNAME);
		keys.add(PersonMap.Key.BIRTHDATE);
		keys.add(PersonMap.Key.PROFESSION);
		keys.add(PersonMap.Key.PHONE);
		keys.add(PersonMap.Key.EMAIL);
		keys.add(PersonMap.Key.WEBSITE);
		keys.add(CourseGuideMap.Key.GUIDE_TYPE);
		keys.add(CourseGuideMap.Key.STATE);
		keys.addAll(PersonMap.getExtendedFieldKeys());
		keys.addAll(LinkMap.getExtendedFieldKeys());
		return keys.toArray(new DataMapKey[0]);
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setCurrentUser();
		final DataMapKey[] keys = getKeys();
		final DataMap<?>[] dataMaps = createDataMaps(selection).toArray(new DataMap<?>[0]);

		super.okPressed();

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					try
					{
						monitor.beginTask("Dokument wird erstellt...", 1);
						buildDocument(new SubProgressMonitor(monitor, dataMaps.length), keys, dataMaps);
						monitor.done();
					}
					finally
					{
						monitor.done();
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			MessageDialog.openError(getShell(), "Fehler",
					"Bei der Verarbeitung ist ein Fehler aufgetreten.\n(" + e.getLocalizedMessage() + ")");
		}
		catch (InterruptedException e)
		{
		}
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

}
