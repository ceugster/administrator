package ch.eugster.events.person.handlers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class DeleteHandler extends AbstractHandler implements IHandler
{
	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public DeleteHandler()
	{
		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null);
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				connectionService = null;
				setBaseEnabled(false);
				super.removedService(reference, service);
			}
		};
		connectionServiceTracker.open();
	}

	private boolean askForDelete(final Shell shell, final StructuredSelection ssel)
	{
		Object[] selectedObjects = ssel.toArray();

		int persons = 0;
		int links = 0;
		int addresses = 0;
		for (Object object : selectedObjects)
		{
			if (object instanceof Person)
			{
				persons++;
			}
			else if (object instanceof LinkPersonAddress)
			{
				links++;
			}
			else if (object instanceof Address)
			{
				addresses++;
			}
		}
		int items = persons + links + addresses;
		if (items == 0)
		{
			return false;
		}

		String msg = null;
		if (items == 1)
		{
			msg = persons == 0 ? "Soll die ausgewählte Adresse entfernt werden?"
					: "Soll die ausgewählte Person entfernt werden?";
		}
		else
		{
			msg = "Sollen die ausgewählten Objekte entfernt werden?";
		}
		String title = " Auswahl entfernen";

		IStatus status = checkObjects(selectedObjects);

		if (status.getSeverity() == IStatus.OK)
		{
			int type = MessageDialog.QUESTION;
			String[] buttons = new String[] { "Ja", "Nein" };
			MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
			return dialog.open() == 0;
		}
		else
		{
			int type = MessageDialog.INFORMATION;
			String[] buttons = new String[] { "OK" };
			MessageDialog dialog = new MessageDialog(shell, title, null, status.getMessage(), type, buttons, 0);
			dialog.open();
			return false;
		}
	}

	private IStatus checkObjects(final Object[] objects)
	{
		for (Object object : objects)
		{
			if (object instanceof Person)
			{
				Person person = (Person) object;
				for (Member member : person.getMembers())
				{
					if (!member.isDeleted())
					{
						return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, PersonFormatter.getInstance()
								.formatFirstnameLastname(person)
								+ " hat eine Mitgliedschaft bei "
								+ member.getMembership().getName() + " und kann nicht gelöscht werden.");
					}
				}

				boolean delete = true;
				for (Donation donation : person.getDonations())
				{
					if (!donation.isDeleted())
					{
						if (donation.getDonationYear() >= GregorianCalendar.getInstance().get(Calendar.YEAR) - 1)
						{
							delete = false;
						}
					}
				}
				if (!delete)
				{
					return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, PersonFormatter.getInstance()
							.formatFirstnameLastname(person)
							+ " hat innerhalb des letzten Jahres gespendet und kann nicht gelöscht werden.");
				}

				for (LinkPersonAddress link : person.getLinks())
				{
					for (Participant participant : link.getParticipants())
					{
						if (participant.getBooking().getCourse().getState().equals(CourseState.FORTHCOMING))
						{
							return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, PersonFormatter.getInstance()
									.formatFirstnameLastname(person)
									+ " hat sich für den bevorstehenden Kurs "
									+ participant.getBooking().getCourse().getTitle()
									+ " angemeldet und kann nicht entfernt werden.");
						}
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getParent().getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty())
			{
				if (askForDelete((Shell) context.getParent().getVariable("activeShell"), ssel))
				{
					Iterator<?> iterator = ssel.iterator();
					while (iterator.hasNext())
					{
						Object object = iterator.next();
						if (connectionService != null)
						{
							if (object instanceof Person)
							{
								Person person = (Person) object;
								PersonQuery query = (PersonQuery) connectionService.getQuery(Person.class);
								object = query.delete(person);
							}
							else if (object instanceof Address)
							{
								Address address = (Address) object;
								AddressQuery query = (AddressQuery) connectionService.getQuery(Address.class);
								object = query.delete(address);
							}
							if (object instanceof LinkPersonAddress)
							{
								LinkPersonAddress link = (LinkPersonAddress) object;
								LinkPersonAddressQuery query = (LinkPersonAddressQuery) connectionService
										.getQuery(LinkPersonAddress.class);
								object = query.delete(link);
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = true;
		if (evaluationContext != null)
		{
			EvaluationContext context = (EvaluationContext) evaluationContext;
			Object object = context.getParent().getVariable("selection");
			if (object instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) object;
				Object[] selected = ssel.toArray();
				for (Object sel : selected)
				{
					if (sel instanceof AbstractEntity)
					{
						if (sel instanceof Person)
						{
							/*
							 * Es handelt sich um den DefaultLink (der wird auf
							 * Person-Ebene für das Anzeigen der Adressdaten
							 * verwendet. Wenn weitere Adressen für die gleiche
							 * Person vorhanden sind, dann muss zuerst die
							 * Default-Adresse geändert werden, bevor der Link
							 * gelöscht werden kann.
							 */
							Person person = (Person) sel;
							if (person.getActiveLinks().size() > 1)
							{
								enabled = false;
								break;
							}
						}
						else if (((AbstractEntity) sel).isDeleted())
						{
							enabled = false;
						}
					}
				}
			}
		}
		super.setBaseEnabled(enabled);
	}
}
