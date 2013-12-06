package ch.eugster.events.person.editors;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.forms.editor.IFormPage;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class AddressContentProposalProvider implements IContentProposalProvider
{
	private final FormEditorLinkPage editorPage;

	public AddressContentProposalProvider(final FormEditorLinkPage editorPage)
	{
		this.editorPage = editorPage;
	}

	private boolean addressExists(final LinkPersonAddress link)
	{
		boolean exists = false;
		PersonEditorInput input = (PersonEditorInput) this.editorPage.getEditor().getEditorInput();
		Person person = input.getEntity();
		if (person.getId() != null && person.getId().equals(link.getPerson().getId()))
		{
			Collection<IFormPage> pages = editorPage.getEditor().getPages();
			for (IFormPage page : pages)
			{
				if (page instanceof FormEditorLinkPage)
				{
					Address address = ((FormEditorLinkPage) page).getLink().getAddress();
					if (address != null)
					{
						if (address.getId() != null)
						{
							if (page != editorPage && link.getAddress().getId().equals(address.getId()))
							{
								exists = true;
							}
						}
					}
				}
			}
		}
		return exists;
	}

	private boolean addressExists(final Address address)
	{
		return false;
	}

	@Override
	public IContentProposal[] getProposals(final String contents, final int position)
	{
		AddressContentProposal[] proposals = new AddressContentProposal[0];
		if (position > 3)
		{
			Map<String, AddressContentProposal> props = new HashMap<String, AddressContentProposal>();
			ConnectionService service = Activator.getDefault().getConnectionService();
			if (service != null)
			{
				LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
				Collection<LinkPersonAddress> links = linkQuery.selectByAddressAsLike(contents);
				Iterator<LinkPersonAddress> linkIterator = links.iterator();
				while (linkIterator.hasNext())
				{
					LinkPersonAddress link = linkIterator.next();
					if (!addressExists(link))
					{
						props.put("L" + link.getId().toString(), new AddressContentProposal(link));
					}
				}
				AddressQuery addressQuery = (AddressQuery) service.getQuery(Address.class);
				Collection<Address> addresses = addressQuery.selectByAddressAsLike(contents);
				Iterator<Address> addressIterator = addresses.iterator();
				while (addressIterator.hasNext())
				{
					Address address = addressIterator.next();
					if (!addressExists(address))
					{
						props.put("A" + address.getId().toString(), new AddressContentProposal(address));
					}
				}
				Address address = Address.newInstance();
				address.setAddress(this.editorPage.getAddress());
				LinkPersonAddress emptyLink = LinkPersonAddress.newInstance(address);
				props.put("L0", new AddressContentProposal(emptyLink));
				proposals = props.values().toArray(new AddressContentProposal[0]);
				Arrays.sort(proposals);
			}
		}
		return proposals;
	}
}
