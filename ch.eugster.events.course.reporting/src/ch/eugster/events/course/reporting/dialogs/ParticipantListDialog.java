package ch.eugster.events.course.reporting.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
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
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class ParticipantListDialog extends TitleAreaDialog
{
	private IDialogSettings settings;

	private Map<CourseState, List<IBookingState>> selectedStates = new HashMap<CourseState, List<IBookingState>>();
	
	private List<Course> courses = new ArrayList<Course>();
	
	private final String message = "Erstellen einer Adressliste der selektierten Kurse.";

	private boolean isPageComplete = false;

	public ParticipantListDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		settings = Activator.getDefault().getDialogSettings().getSection("participant.list.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("participant.list.dialog");
		}
		Object[] elements = selection.toArray();
		for (Object element : elements)
		{
			if (element instanceof Season)
			{
				Season season = (Season) element;
				if (!season.isDeleted())
				{
					List<Course> courses = season.getCourses();
					for (Course course : courses)
					{
						if (!course.isDeleted())
						{
							this.courses.add(course);
						}
					}
				}
			}
			else if (element instanceof Course)
			{
				this.courses.add((Course) element);
			}
		}
		for (Course course : this.courses)
		{
			if (this.selectedStates.get(course.getState()) == null)
			{
				this.selectedStates.put(course.getState(), new ArrayList<IBookingState>());
			}
		}
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(this.selectedStates.size(), true));

		int max = 0;
		for (CourseState courseState : this.selectedStates.keySet())
		{
			max = Math.max(max, courseState.getBookingStates().length);
		}
		Arrays.sort(this.selectedStates.keySet().toArray(new CourseState[0]), new Comparator<CourseState>() 
		{
			@Override
			public int compare(CourseState o1, CourseState o2) 
			{
				return - o2.ordinal() - o1.ordinal();
			} 
		});

		for (CourseState cst : this.selectedStates.keySet())
		{
			final CourseState courseState = cst;
			
			Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setLayout(new GridLayout());
			group.setText(courseState.toString());

			for (int i = 0; i < max; i++)
			{
				if (i < courseState.getBookingStates().length)
				{
					final IBookingState bookingState = courseState.getBookingStates()[i];
					boolean selected = settings.getBoolean(bookingState.name());
					if (selected)
					{
						List<IBookingState> bookingStates = this.selectedStates.get(courseState);
						if (!bookingStates.contains(bookingState)) 
						{
							bookingStates.add(bookingState);
						}
					}
					else
					{
						List<IBookingState> bookingStates = this.selectedStates.get(courseState);
						if (bookingStates.contains(bookingState)) 
						{
							bookingStates.remove(bookingState);
						}
					}
					final Button button = new Button(group, SWT.CHECK);
					button.setText(bookingState.toString());
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
							if (button.getSelection())
							{
								List<IBookingState> bookingStates = selectedStates.get(courseState);
								if (!bookingStates.contains(bookingState)) 
								{
									bookingStates.add(bookingState);
								}
							}
							else
							{
								List<IBookingState> bookingStates = selectedStates.get(courseState);
								if (bookingStates.contains(bookingState)) 
								{
									bookingStates.remove(bookingState);
								}
							}
						}
					});
				}
				else
				{
					Label label = new Label(group, SWT.None);
					label.setLayoutData(new GridData());
				}
			}
		}

		return parent;
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
		if (!booking.isDeleted() && this.selectedStates.get(booking.getCourse().getState()).contains(booking.getState()))
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

	private List<DataMap<?>> createDataMaps()
	{
		List<DataMap<?>> maps = new ArrayList<DataMap<?>>();
		for (Course course : courses)
		{
			computeCourse(maps, course);
		}
		return maps;
	}

	private DataMapKey[] getKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(AddressMap.Key.NAME);
		keys.add(LinkMap.Key.MAILING_ADDRESS);
		keys.add(ParticipantMap.Key.BOOKING_TYPE_CODE);
		keys.add(ParticipantMap.Key.BOOKING_TYPE_NAME);
		keys.add(ParticipantMap.Key.PRICE);
		keys.add(ParticipantMap.Key.COUNT);
		keys.add(ParticipantMap.Key.ID);
		keys.add(PersonMap.Key.FORM);
		keys.add(PersonMap.Key.SEX);
		keys.add(ParticipantMap.Key.SALUTATION);
		keys.add(PersonMap.Key.FIRSTNAME);
		keys.add(PersonMap.Key.LASTNAME);
		keys.add(AddressMap.Key.ADDRESS);
		keys.add(AddressMap.Key.POB);
		keys.add(AddressMap.Key.COUNTRY);
		keys.add(AddressMap.Key.ZIP);
		keys.add(AddressMap.Key.CITY);
		keys.add(AddressMap.Key.COUNTY);
		keys.add(PersonMap.Key.PHONE);
		keys.add(LinkMap.Key.PHONE);
		keys.add(AddressMap.Key.PHONE);
		keys.add(PersonMap.Key.EMAIL);
		keys.add(PersonMap.Key.BIRTHDATE);
		keys.add(PersonMap.Key.PROFESSION);
//		keys.add(AddressMap.Key.FAX);
//		keys.add(ParticipantMap.Key.ANOTHER_LINE);
//		keys.add(ParticipantMap.Key.POLITE);
//		keys.add(PersonMap.Key.TITLE);
//		keys.add(PersonMap.Key.WEBSITE);
//		keys.add(CourseGuideMap.Key.GUIDE_TYPE);
//		keys.add(CourseGuideMap.Key.STATE);
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
		final DataMap<?>[] dataMaps = createDataMaps().toArray(new DataMap<?>[0]);

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
