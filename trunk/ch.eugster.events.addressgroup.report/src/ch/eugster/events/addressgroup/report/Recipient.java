package ch.eugster.events.addressgroup.report;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class Recipient implements Comparable<Recipient>
{
	private String id;

	private String name;

	private String address;

	private String city;

	private String phone;

	private String email;

	private String code;

	public Recipient()
	{
		super();
	}

	/**
	 * Load Address
	 * 
	 * @param member
	 */
	public Recipient(final AddressGroupMember member)
	{
		this.code = member.getCopiedFrom() == null ? "" : member.getCopiedFrom().getCode();
		if (member.isValidAddressMember())
		{
			loadData(member.getAddress());
		}
		else if (member.isValidLinkMember())
		{
			loadData(member.getLink());
		}
	}

	public Recipient(final AddressGroupMember member, final Address address)
	{
		this.code = member.getCopiedFrom() == null ? "" : member.getCopiedFrom().getCode();
		loadData(member.getLink(), address);
	}

	public Recipient(final AddressGroupMember member, final Person person)
	{
		this.code = member.getCopiedFrom() == null ? "" : member.getCopiedFrom().getCode();
		loadData(member.getLink(), person);
	}

	@Override
	public int compareTo(final Recipient other)
	{
		Recipient recipient = other;
		int comparison = this.getName().compareTo(recipient.getName());
		if (comparison == 0)
		{
			return this.getId().compareTo(recipient.getId());
		}
		return comparison;
	}

	public String getAddress()
	{
		return address;
	}

	public String getCity()
	{
		return city;
	}

	public String getCode()
	{
		return code;
	}

	public String getEmail()
	{
		return email;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getPhone()
	{
		return phone;
	}

	private void loadData(final Address address)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(address);
		this.city = AddressFormatter.getInstance().formatCityLine(address);
		this.email = address.getEmail();
		this.id = AddressFormatter.getInstance().formatId(address);
		this.name = address.getName();
		this.phone = AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(address.getCountry(),
				address.getPhone());
	}

	private void loadData(final LinkPersonAddress link)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(link.getAddress());
		this.city = AddressFormatter.getInstance().formatCityLine(link.getAddress());
		this.email = link.getEmail();
		this.id = PersonFormatter.getInstance().formatId(link.getPerson());
		this.name = PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
		this.phone = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPhone());
	}

	private void loadData(final LinkPersonAddress link, final Address address)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(address);
		this.city = AddressFormatter.getInstance().formatCityLine(address);
		this.email = link.getAddress().getEmail();
		this.id = PersonFormatter.getInstance().formatId(link.getPerson());
		this.name = PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
		this.phone = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getAddress().getCountry(),
				link.getAddress().getPhone());
	}

	private void loadData(final LinkPersonAddress link, final Person person)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(link.getAddress());
		this.city = AddressFormatter.getInstance().formatCityLine(link.getAddress());
		this.email = link.getPerson().getEmail();
		this.id = PersonFormatter.getInstance().formatId(link.getPerson());
		this.name = PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
		this.phone = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPerson().getPhone());
	}

	public void setAddress(final String address)
	{
		this.address = address;
	}

	public void setCity(final String city)
	{
		this.city = city;
	}

	public void setCode(final String code)
	{
		this.code = code;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setPhone(final String phone)
	{
		this.phone = phone;
	}
}
