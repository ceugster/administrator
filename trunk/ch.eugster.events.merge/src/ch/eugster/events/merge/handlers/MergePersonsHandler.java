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
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class MergePersonsHandler extends AbstractHandler implements IHandler
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
				merge(personView.getViewer());
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

	private void merge(final TableViewer viewer)
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
			}
		}
		if (selectedLink != null)
		{
			for (Object object : objects)
			{
				if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					if (!link.getId().equals(selectedLink.getId()))
					{
						AddressGroupMember[] addressGroupMembers = link.getAddressGroupMembers().toArray(
								new AddressGroupMember[0]);
						for (AddressGroupMember addressGroupMember : addressGroupMembers)
						{
							if (!exists(selectedLink, addressGroupMember))
							{
								link.removeAddressGroupMember(addressGroupMember);
								addressGroupMember.setParent(selectedLink, selectedLink.getAddress());
								selectedLink.addAddressGroupMember(addressGroupMember);
							}
						}
						Donation[] donations = link.getDonations().toArray(new Donation[0]);
						for (Donation donation : donations)
						{
							link.removeDonation(donation);
							donation.setLink(selectedLink);
							selectedLink.addDonation(donation);
						}
						Member[] members = link.getMembers().toArray(new Member[0]);
						for (Member member : members)
						{
							link.removeMember(member);
							member.setLink(selectedLink);
							selectedLink.addMember(member);
						}
						if (link.getGuide() != null)
						{
							Guide guide = link.getGuide();
							guide.setLink(selectedLink);
							selectedLink.setGuide(guide);
						}
						Participant[] participants = link.getParticipants().toArray(new Participant[0]);
						for (Participant participant : participants)
						{
							participant.setLink(selectedLink);
							selectedLink.addParticipant(participant);
						}

						ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle()
								.getBundleContext(), ConnectionService.class.getName(), null);
						tracker.open();
						ConnectionService service = (ConnectionService) tracker.getService();
						if (service != null)
						{
							LinkPersonAddressQuery query = (LinkPersonAddressQuery) service
									.getQuery(LinkPersonAddress.class);
							link.setDeleted(true);
							link.getPerson().setDeleted(true);
							link = query.merge(link);
							selectedLink = query.merge(selectedLink);
						}
						tracker.close();
					}
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
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				setBaseEnabled(ssel.size() > 1);
			}
		}
	}
}
