package ch.eugster.events.merge.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.merge.Activator;
import ch.eugster.events.merge.views.PersonView;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class SetToAddressHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("activePart") instanceof PersonView)
			{
				PersonView personView = (PersonView) context.getParent().getVariable("activePart");
				setToAddress(personView.getViewer());
			}
		}
		return null;
	}

	private boolean exists(final LinkPersonAddress selectedLink, final AddressGroupMember member)
	{
		LinkPersonAddress[] links = selectedLink.getPerson().getLinks().toArray(new LinkPersonAddress[0]);
		for (LinkPersonAddress link : links)
		{
			if (link.getAddressGroupMembers().contains(member))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return super.isEnabled();
	}

	private void setToAddress(final TableViewer viewer)
	{
		LinkPersonAddress selectedLink = null;
		StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
		Object[] objects = ssel.toArray();
		for (Object object : objects)
		{
			TableItem item = (TableItem) viewer.testFindItem(object);
			if (item.getChecked())
			{
				selectedLink = (LinkPersonAddress) item.getData();
				if (selectedLink != null)
				{
					AddressGroupMember[] addressGroupMembers = selectedLink.getAddressGroupMembers().toArray(
							new AddressGroupMember[0]);
					for (AddressGroupMember addressGroupMember : addressGroupMembers)
					{
						selectedLink.removeAddressGroupMember(addressGroupMember);
						if (!addressGroupMember.isDeleted())
						{
							addressGroupMember.setParent(null, selectedLink.getAddress());
						}
					}
					Donation[] donations = selectedLink.getDonations().toArray(new Donation[0]);
					for (Donation donation : donations)
					{
						selectedLink.removeDonation(donation);
						donation.setAddress(selectedLink.getAddress());
					}
					Member[] members = selectedLink.getMembers().toArray(new Member[0]);
					for (Member member : members)
					{
						selectedLink.removeMember(member);
						member.setLink(selectedLink);
					}
					if (selectedLink.getGuide() != null)
					{
						selectedLink.getGuide().setDeleted(true);
					}
					Participant[] participants = selectedLink.getParticipants().toArray(new Participant[0]);
					for (Participant participant : participants)
					{
						participant.setDeleted(true);
					}

					ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
							ConnectionService.class.getName(), null);
					tracker.open();
					ConnectionService service = (ConnectionService) tracker.getService();
					if (service != null)
					{
						LinkPersonAddressQuery query = (LinkPersonAddressQuery) service
								.getQuery(LinkPersonAddress.class);
						if (selectedLink.getPerson().getValidLinks().size() == 1)
						{
							selectedLink.getPerson().setDeleted(true);
						}
						selectedLink.setDeleted(true);
						selectedLink.getAddress().setDeleted(false);
						selectedLink.getAddress().removeAddressLink(selectedLink);
						selectedLink = query.merge(selectedLink);
						AddressQuery addressQuery = (AddressQuery) service.getQuery(Address.class);
						addressQuery.merge(selectedLink.getAddress());
					}
					tracker.close();
				}

			}
		}
	}

	@Override
	public void setEnabled(final Object object)
	{
		if (object instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) object;
			if (context.getParent().getVariable("activePart") instanceof PersonView)
			{
				PersonView personView = (PersonView) context.getParent().getVariable("activePart");
				TableViewer viewer = personView.getViewer();
				TableItem[] items = viewer.getTable().getItems();
				for (TableItem item : items)
				{
					if (item.getChecked())
					{
						setBaseEnabled(true);
						return;
					}
				}
			}
		}
		setBaseEnabled(false);
	}
}
