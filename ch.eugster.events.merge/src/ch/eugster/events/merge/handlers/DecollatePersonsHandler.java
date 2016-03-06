package ch.eugster.events.merge.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.PlatformUI;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.merge.Activator;
import ch.eugster.events.merge.views.PersonView;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DecollatePersonsHandler extends AbstractHandler implements IHandler
{
	private void decollate(final TableViewer viewer, final int count)
	{
		StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
		if (ssel.getFirstElement() instanceof LinkPersonAddress)
		{
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
			tracker.open();
			try
			{
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
	
					LinkPersonAddress selectedLink = (LinkPersonAddress) ssel.getFirstElement();
					Address address = selectedLink.getAddress();
					for (int i = 0; i < count; i++)
					{
						Person newPerson = selectedLink.getPerson().copy();
						LinkPersonAddress link = LinkPersonAddress.newInstance(newPerson, address);
						link.setAddressType(selectedLink.getAddressType());
						AddressGroupMember[] addressGroupMembers = selectedLink.getAddressGroupMembers().toArray(
								new AddressGroupMember[0]);
						for (AddressGroupMember addressGroupMember : addressGroupMembers)
						{
							AddressGroupMember newMember = addressGroupMember.copy(addressGroupMember.getAddressGroup());
							newMember.setParent(addressGroupMember.getLink(), addressGroupMember.getAddress());
						}
						Member[] members = link.getMembers().toArray(new Member[0]);
						for (Member member : members)
						{
							link.removeMember(member);
							member.setLink(selectedLink);
						}
						Donation[] donations = selectedLink.getDonations().toArray(new Donation[0]);
						for (Donation donation : donations)
						{
							selectedLink.removeDonation(donation);
							selectedLink.getAddress().addDonation(donation);
						}
						if (selectedLink.getGuide() != null)
						{
							Guide newGuide = Guide.newInstance(link);
							newGuide.setDescription(selectedLink.getGuide().getDescription());
							newGuide.setPhone(selectedLink.getGuide().getPhone());
							link.setGuide(newGuide);
						}
						newPerson.setDefaultLink(link);
						link = query.merge(link);
					}
				}
			}
			finally
			{
				tracker.close();
			}
		}
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("activePart") instanceof PersonView)
			{
				PersonView personView = (PersonView) context.getParent().getVariable("activePart");
				if (((StructuredSelection) personView.getViewer().getSelection()).size() == 1)
				{

					InputDialog dialog = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), "Anzahl Personen",
							"Wieviele zusätzliche Personen sollen aus der ausgewählten Adresse generiert werden?",
							Integer.valueOf(1).toString(), new IInputValidator()
							{
								@Override
								public String isValid(final String value)
								{
									try
									{
										int val = Integer.valueOf(value).intValue();
										return (val > 0 && val <= Integer.MAX_VALUE) ? null
												: "Der eingegebene Wert liegt nicht im gültigen Zahlenbereich";
									}
									catch (NumberFormatException e)
									{
										return "Der eingegebene Wert ist keine gültige Zahl";
									}
								}

							});
					if (dialog.open() == IDialogConstants.OK_ID)
					{
						int count = Integer.valueOf(dialog.getValue());
						decollate(personView.getViewer(), count);
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled()
	{
		return super.isEnabled();
	}

	@Override
	public void setEnabled(final Object object)
	{
		if (object instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) object;
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				setBaseEnabled(ssel.size() == 1);
			}
		}
	}
}
