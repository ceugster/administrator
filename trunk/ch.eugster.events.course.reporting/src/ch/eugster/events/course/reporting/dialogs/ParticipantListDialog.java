package ch.eugster.events.course.reporting.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

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
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.maps.ParticipantMap;
import ch.eugster.events.documents.maps.PersonMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class ParticipantListDialog extends TitleAreaDialog
{
	// private Button collectionSelector;

	private final StructuredSelection selection;

	private final String message = "Erstellen einer Adressliste der selektierten Kurse.";

	private boolean isPageComplete = false;

	public ParticipantListDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private void buildDocument(IProgressMonitor monitor, final DataMapKey[] keys, final Collection<DataMap> dataMaps)
	{
		IStatus status = Status.CANCEL_STATUS;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class.getName(), null);
		try
		{
			tracker.open();
			Object[] services = tracker.getServices();
			if (services != null)
			{
				try
				{
					monitor.beginTask("Dokument wird erstellt...", services.length);
					for (Object service : services)
					{
						if (status.isOK())
						{
							monitor.worked(1);
							return;
						}
						if (service instanceof DocumentBuilderService)
						{
							DocumentBuilderService builderService = (DocumentBuilderService) service;
							status = builderService.buildDocument(new SubProgressMonitor(monitor, dataMaps.size()),
									keys, dataMaps);
							if (status.isOK())
							{
								break;
							}
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

	private void computeBooking(final Collection<DataMap> map, final Booking booking)
	{
		if (!booking.isDeleted())
		{
			Collection<Participant> participants = booking.getParticipants();
			for (Participant participant : participants)
			{
				computeParticipant(map, participant);
			}
		}
	}

	private void computeSeason(final Collection<DataMap> map, final Season season)
	{
		if (!season.isDeleted())
		{
			Collection<Course> courses = season.getCourses();
			for (Course course : courses)
			{
				computeCourse(map, course);
			}
		}
	}

	private void computeCourse(final Collection<DataMap> map, final Course course)
	{
		if (!course.isDeleted())
		{
			Collection<Booking> bookings = course.getBookings();
			for (Booking booking : bookings)
			{
				computeBooking(map, booking);
			}
		}
	}

	private void computeParticipant(final Collection<DataMap> map, final Participant participant)
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

	private Collection<DataMap> createDataMaps(final StructuredSelection ssel)
	{
		Collection<DataMap> maps = new ArrayList<DataMap>();
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
		// collectionSelector = new Button(composite, SWT.CHECK);
		// collectionSelector.setText("Gruppenadressen nur einmal auflisten");
		// collectionSelector.setLayoutData(new GridData());
		// }

		return parent;
	}

	private DataMapKey[] getKeys()
	{
		Collection<DataMapKey> keys = new ArrayList<DataMapKey>();
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
		final Collection<DataMap> dataMaps = createDataMaps(selection);

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
						buildDocument(new SubProgressMonitor(monitor, dataMaps.size()), keys, dataMaps);
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
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			UserQuery query = (UserQuery) service.getQuery(User.class);
			User.setCurrent(query.merge(User.getCurrent()));
		}
		tracker.close();
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
