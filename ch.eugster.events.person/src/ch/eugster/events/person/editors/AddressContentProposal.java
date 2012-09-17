package ch.eugster.events.person.editors;

import org.eclipse.jface.fieldassist.IContentProposal;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.LinkPersonAddress;

public class AddressContentProposal implements IContentProposal, Comparable<AddressContentProposal>
{
	private final LinkPersonAddress link;

	public AddressContentProposal(final LinkPersonAddress link)
	{
		this.link = link;
	}

	@Override
	public int compareTo(final AddressContentProposal other)
	{
		if (other instanceof AddressContentProposal)
		{
			if (this.getPersonAddressLink().getId() == null)
			{
				return -1;
			}
			else if (other.getPersonAddressLink().getId() == null)
			{
				return 1;
			}
			return this.getContent().compareTo(other.getContent());
		}
		return 0;
	}

	@Override
	public String getContent()
	{
		return this.link.getAddress().getAddress();
	}

	@Override
	public int getCursorPosition()
	{
		return 0;
	}

	@Override
	public String getDescription()
	{
		return "Adressen zur Auswahl";
	}

	@Override
	public String getLabel()
	{
		StringBuilder builder = new StringBuilder(this.link.getAddress().getAddress());
		if (!this.link.getAddress().getCity().isEmpty())
		{
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(AddressFormatter.getInstance().formatCityLine(this.link.getAddress()));
		}
		if (this.link.getPerson() != null)
		{
			if (!this.link.getPerson().getLastname().isEmpty())
			{
				if (builder.length() > 0)
					builder.append(", ");
				builder.append(PersonFormatter.getInstance().formatLastnameFirstname(this.link.getPerson()));
			}
		}
		return builder.toString();
	}

	public LinkPersonAddress getPersonAddressLink()
	{
		return this.link;
	}

}
