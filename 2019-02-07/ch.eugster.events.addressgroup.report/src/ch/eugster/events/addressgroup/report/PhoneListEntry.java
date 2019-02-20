package ch.eugster.events.addressgroup.report;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class PhoneListEntry implements Comparable<PhoneListEntry>
{
	private String id;

	private String name;

	private String address;

	private String city;

	private String phonePrivate;

	private String phoneMobile;

	private String phoneBusiness;

	private String email;

	private String website;

	public PhoneListEntry()
	{
		super();
	}

	/**
	 * Load Address
	 * 
	 * @param member
	 */
	public PhoneListEntry(final AddressGroupMember member)
	{
		if (member.isValidAddressMember())
		{
			loadData(member.getAddress());
		}
		else if (member.isValidLinkMember())
		{
			loadData(member.getLink());
		}
	}

	public PhoneListEntry(final AddressGroupMember member, final Address address)
	{
		loadData(member.getLink(), address);
	}

	public PhoneListEntry(final AddressGroupMember member, final Person person)
	{
		loadData(member.getLink(), person);
	}

	@Override
	public int compareTo(final PhoneListEntry other)
	{
		int comparison = this.getName().compareTo(other.getName());
		if (comparison == 0)
		{
			return this.getId().compareTo(other.getId());
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

	public String getPhoneBusiness()
	{
		return phoneBusiness;
	}

	public String getPhoneMobile()
	{
		return phoneMobile;
	}

	public String getPhonePrivate()
	{
		return phonePrivate;
	}

	public String getWebsite()
	{
		return website;
	}

	private void loadData(final Address address)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(address);
		this.city = AddressFormatter.getInstance().formatCityLine(address);
		this.email = address.getEmail();
		this.id = AddressFormatter.getInstance().formatId(address);
		this.name = address.getName();
		this.phoneBusiness = AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(address.getCountry(),
				address.getPhone());
	}

	private void loadData(final LinkPersonAddress link)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(link.getAddress());
		this.city = AddressFormatter.getInstance().formatCityLine(link.getAddress());
		this.id = PersonFormatter.getInstance().formatId(link.getPerson());
		this.name = PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
		this.phonePrivate = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPhone());
		this.phoneMobile = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPerson().getPhone());
		this.phoneBusiness = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getAddress().getPhone());
		this.email = link.getEmail();
		this.website = link.getPerson().getWebsite();
	}

	private void loadData(final LinkPersonAddress link, final Address address)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(address);
		this.city = AddressFormatter.getInstance().formatCityLine(address);
		this.id = PersonFormatter.getInstance().formatId(link.getPerson());
		this.name = PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
		this.phonePrivate = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPhone());
		this.phoneMobile = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPerson().getPhone());
		this.phoneBusiness = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getAddress().getPhone());
		this.email = link.getEmail();
		this.website = link.getPerson().getWebsite();
	}

	private void loadData(final LinkPersonAddress link, final Person person)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(link.getAddress());
		this.city = AddressFormatter.getInstance().formatCityLine(link.getAddress());
		this.email = link.getPerson().getEmail();
		this.id = PersonFormatter.getInstance().formatId(link.getPerson());
		this.name = PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
		this.phonePrivate = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPhone());
		this.phoneMobile = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getPerson().getPhone());
		this.phoneBusiness = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
				link.getAddress().getPhone());
		this.email = link.getEmail();
		this.website = link.getPerson().getWebsite();
	}

	public void setAddress(final String address)
	{
		this.address = address;
	}

	public void setCity(final String city)
	{
		this.city = city;
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

	public void setPhoneBusiness(final String phone)
	{
		this.phoneBusiness = phone;
	}

	public void setPhoneMobile(final String phone)
	{
		this.phoneMobile = phone;
	}

	public void setPhonePrivate(final String phone)
	{
		this.phonePrivate = phone;
	}

	public void setWebsite(final String website)
	{
		this.website = website;
	}
}
