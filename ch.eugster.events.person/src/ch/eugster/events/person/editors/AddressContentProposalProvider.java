package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.forms.editor.IFormPage;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class AddressContentProposalProvider implements IContentProposalProvider
{
	private final FormEditorLinkPage editorPage;

	public AddressContentProposalProvider(FormEditorLinkPage editorPage)
	{
		this.editorPage = editorPage;
	}

	private boolean addressExists(LinkPersonAddress link)
	{
		boolean exists = false;
		Person person = this.editorPage.getLink().getPerson();
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

	@Override
	public IContentProposal[] getProposals(String contents, int position)
	{
		AddressContentProposal[] proposals = new AddressContentProposal[0];
		if (position > 3)
		{
			Collection<AddressContentProposal> props = new ArrayList<AddressContentProposal>();
			ConnectionService service = Activator.getDefault().getConnectionService();
			if (service != null)
			{
				LinkPersonAddressQuery query = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
				Collection<LinkPersonAddress> links = query.selectByAddressAsLike(contents);
				Iterator<LinkPersonAddress> iterator = links.iterator();
				for (int i = 1; iterator.hasNext(); i++)
				{
					LinkPersonAddress link = iterator.next();
					if (!addressExists(link))
					{
						props.add(new AddressContentProposal(link));
					}
				}
				Address address = Address.newInstance();
				address.setAddress(this.editorPage.getAddress());
				LinkPersonAddress emptyLink = LinkPersonAddress.newInstance(address);
				props.add(new AddressContentProposal(emptyLink));
				proposals = props.toArray(new AddressContentProposal[0]);
				Arrays.sort(proposals);
			}
		}
		return proposals;
	}
}
