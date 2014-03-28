package ch.eugster.events.person.editors;

import org.eclipse.jface.fieldassist.IContentProposal;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;

public class AddressContentProposal implements IContentProposal, Comparable<AddressContentProposal>
{
	private LinkPersonAddress link;

	private Address address;

	public AddressContentProposal(final LinkPersonAddress link)
	{
		this.link = link;
		this.address = link.getAddress();
	}

	public AddressContentProposal(final Address address)
	{
		this.address = address;
	}

	@Override
	public int compareTo(final AddressContentProposal other)
	{
		if (other instanceof AddressContentProposal)
		{
			if (this.link == null || this.link.isDeleted() || this.link.getPerson().isDeleted())
			{
				if (this.address.getId() == null)
				{
					return -1;
				}
				else if (other.getAddress().getId() == null)
				{
					return 1;
				}
			}
			else
			{
				if (this.link.getId() == null)
				{
					return -1;
				}
				else if (other.getAddress().getId() == null)
				{
					return 1;
				}
			}
			return this.getContent().compareTo(other.getContent());
		}
		return 0;
	}

	@Override
	public String getContent()
	{
		return this.link == null || this.link.isDeleted() || this.link.getPerson().isDeleted() ? this.address
				.getAddress() : this.link.getAddress().getAddress();
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
		StringBuilder builder = new StringBuilder(AddressFormatter.getInstance().formatId(this.address));
		builder = builder.append(", " + this.address.getAddress());
		if (!this.address.getCity().isEmpty())
		{
			if (builder.length() > 0)
			{
				builder.append(", ");
			}
			builder.append(AddressFormatter.getInstance().formatCityLine(this.address));
		}
		if (this.link == null || this.link.isDeleted() || this.link.getPerson().isDeleted())
		{
			if (!this.address.getName().isEmpty())
			{
				if (builder.length() > 0)
				{
					builder.append(", ");
				}
				builder.append(this.address.getName());
			}
		}
		else
		{
			if (this.link.getPerson() != null)
			{
				if (!this.link.getPerson().getLastname().isEmpty())
				{
					if (builder.length() > 0)
						builder.append(", ");
					builder.append(PersonFormatter.getInstance().formatLastnameFirstname(this.link.getPerson()));
				}
			}
		}
		return builder.toString();
	}

	public LinkPersonAddress getPersonAddressLink()
	{
		return this.link;
	}

	public Address getAddress()
	{
		return this.address;
	}
}
