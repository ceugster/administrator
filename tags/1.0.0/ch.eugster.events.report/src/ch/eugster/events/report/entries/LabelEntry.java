package ch.eugster.events.report.entries;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class LabelEntry implements Comparable<LabelEntry>
{
	private LinkPersonAddress link;

	private Address address;

	private Person person;

	private String label;

	public LabelEntry()
	{
		super();
	}

	/**
	 * Load Address
	 * 
	 * @param member
	 */
	public LabelEntry(final Address address)
	{
		loadData(address);
	}

	public LabelEntry(final AddressGroupMember member, final Address address)
	{
		loadData(member.getLink(), address);
	}

	public LabelEntry(final AddressGroupMember member, final Person person)
	{
		loadData(member.getLink(), person);
	}

	public LabelEntry(final LinkPersonAddress link)
	{
		loadData(link);
	}

	@Override
	public int compareTo(final LabelEntry other)
	{
		int comparison = this.getOrder().compareTo(other.getOrder());
		if (comparison == 0)
		{
			return this.getId().compareTo(other.getId());
		}
		return comparison;
	}

	public String getId()
	{
		return link == null ? "A" + this.address.getId().toString() : "L" + link.getId().toString();
	}

	public String getLabel()
	{
		return link == null ? AddressFormatter.getInstance().getLabel(address) : LinkPersonAddressFormatter
				.getInstance().getLabel(link);
	}

	public String getOrder()
	{
		return link == null ? address.getName() : PersonFormatter.getInstance().formatLastnameFirstname(person);
	}

	private void loadData(final Address address)
	{
		this.address = address;
		this.label = AddressFormatter.getInstance().createVisibleAddressLabel();

	}

	private void loadData(final LinkPersonAddress link)
	{
		this.person = link.getPerson();
		loadData(link, link.getAddress());
	}

	private void loadData(final LinkPersonAddress link, final Address address)
	{
		this.link = link;
		this.address = link.getAddress();
		this.label = LinkPersonAddressFormatter.getInstance().getLabel(link);
	}

	private void loadData(final LinkPersonAddress link, final Person person)
	{
		this.person = person;
		loadData(link, link.getAddress());
	}
}