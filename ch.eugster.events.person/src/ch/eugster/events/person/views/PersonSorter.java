package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class PersonSorter extends ViewerSorter
{
	private ViewerColumn currentColumn = ViewerColumn.LASTNAME;

	private boolean ascending = true;

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		return currentColumn.compare(viewer, e1, e2, ascending);
	}

	public ViewerColumn getCurrentColumn()
	{
		return currentColumn;
	}

	public boolean isAscending()
	{
		return ascending;
	}

	public void setAscending(final boolean ascending)
	{
		this.ascending = ascending;
	}

	public void setCurrentColumn(final ViewerColumn column)
	{
		if (column == this.currentColumn)
		{
			this.ascending = !this.ascending;
		}
		else
		{
			this.currentColumn = column;
		}
	}

	public enum ViewerColumn
	{
		CODE, NAME, LASTNAME, FIRSTNAME, ADDRESS_ID, ORGANISATION, ADDRESS, CITY, MOBILE, PHONE, FAX, EMAIL, DOMAIN;

		public String label()
		{
			switch (this)
			{
				case CODE:
				{
					return "Code";
				}
				case NAME:
				{
					return "Name";
				}
				case LASTNAME:
				{
					return "Nachname";
				}
				case FIRSTNAME:
				{
					return "Vorname";
				}
				case ADDRESS_ID:
				{
					return "Adresse";
				}
				case ORGANISATION:
				{
					return "Organisation";
				}
				case ADDRESS:
				{
					return "Strasse";
				}
				case CITY:
				{
					return "Ort";
				}
				case MOBILE:
				{
					return "Mobile";
				}
				case PHONE:
				{
					return "Telefon";
				}
				case FAX:
				{
					return "Fax";
				}
				case EMAIL:
				{
					return "Email";
				}
				case DOMAIN:
				{
					return "Domäne";
				}
				default:
					return "";
			}
		}

		public String value(Person person)
		{
			switch (this)
			{
				case CODE:
				{
					return PersonFormatter.getInstance().formatId(person);
				}
				case NAME:
				{
					return PersonFormatter.getInstance().formatLastnameFirstname(person);
				}
				case LASTNAME:
				{
					return person.getLastname();
				}
				case FIRSTNAME:
				{
					return person.getFirstname();
				}
				case ADDRESS_ID:
				{
					return person.getDefaultLink().getAddress().getId().toString();
				}
				case ORGANISATION:
				{
					return person.getDefaultLink().getAddress().getName();
				}
				case ADDRESS:
				{
					return AddressFormatter.getInstance().formatAddressLine(person.getDefaultLink().getAddress());
				}
				case CITY:
				{
					return AddressFormatter.getInstance().formatCityLine(person.getDefaultLink().getAddress());
				}
				case PHONE:
				{
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(person.getCountry(),
							person.getDefaultLink().getPhone());
				}
				case MOBILE:
				{
					return PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(person.getCountry(),
							person.getPhone());
				}
				case FAX:
				{
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(person.getDefaultLink().getAddress().getCountry(),
							person.getDefaultLink().getAddress().getFax());
				}
				case EMAIL:
				{
					String email = person.getDefaultLink().getEmail();
					if (email.isEmpty())
					{
						email = person.getEmail();
					}
					if (email.isEmpty())
					{
						email = person.getDefaultLink().getAddress().getEmail();
					}
					return email;
				}
				case DOMAIN:
				{
					return person.getDomain() == null ? "" : person.getDomain().getName();
				}
				default:
				{
					return "";
				}
			}
		}

		public String value(LinkPersonAddress link)
		{
			switch (this)
			{
				case CODE:
				{
					return PersonFormatter.getInstance().formatId(link.getPerson());
				}
				case NAME:
				{
					return PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
				}
				case LASTNAME:
				{
					return link.getPerson().getLastname();
				}
				case FIRSTNAME:
				{
					return link.getPerson().getFirstname();
				}
				case ADDRESS_ID:
				{
					return link.getAddress().getId().toString();
				}
				case ORGANISATION:
				{
					return link.getAddress().getName();
				}
				case ADDRESS:
				{
					return AddressFormatter.getInstance().formatAddressLine(link.getAddress());
				}
				case CITY:
				{
					return AddressFormatter.getInstance().formatCityLine(link.getAddress());
				}
				case PHONE:
				{
					return PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
							link.getPerson().getPhone());
				}
				case MOBILE:
				{
					return PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getPerson().getCountry(),
							link.getPhone());
				}
				case FAX:
				{
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(link.getAddress().getCountry(),
							link.getAddress().getFax());
				}
				case EMAIL:
				{
					String email = link.getEmail();
					if (email.isEmpty())
					{
						email = link.getPerson().getEmail();
					}
					if (email.isEmpty())
					{
						email = link.getAddress().getEmail();
					}
					return email;
				}
				case DOMAIN:
				{
					return link.getPerson().getDomain() == null ? "" : link.getPerson().getDomain().getName();
				}
				default:
				{
					return "";
				}
			}
		}

		public String value(Address address)
		{
			switch (this)
			{
				case CODE:
				{
					return "";
				}
				case NAME:
				{
					return "";
				}
				case LASTNAME:
				{
					return "";
				}
				case FIRSTNAME:
				{
					return "";
				}
				case ADDRESS_ID:
				{
					return address.getId().toString();
				}
				case ORGANISATION:
				{
					return address.getName();
				}
				case ADDRESS:
				{
					return AddressFormatter.getInstance().formatAddressLine(address);
				}
				case CITY:
				{
					return AddressFormatter.getInstance().formatCityLine(address);
				}
				case PHONE:
				{
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(address.getCountry(),
							address.getPhone());
				}
				case MOBILE:
				{
					return "";
				}
				case FAX:
				{
					return AddressFormatter.getInstance().formatPhoneWithOptionalPrefix(address.getCountry(),
							address.getFax());
				}
				case EMAIL:
				{
					String email = address.getEmail();
					return email;
				}
				case DOMAIN:
				{
					return "";
				}
				default:
				{
					return "";
				}
			}
		}

		public int compare(final Viewer viewer, final Object e1, final Object e2, boolean ascending)
		{
			if (e1 instanceof Person)
			{
				Person p1 = (Person) e1;
				if (e2 instanceof Person)
				{
					Person p2 = (Person) e2;
					return compare(p1, p2, ascending);
				}
				else if (e2 instanceof LinkPersonAddress)
				{
					LinkPersonAddress l2 = (LinkPersonAddress) e2;
					return compare(p1, l2.getPerson(), ascending);
				}
				else if (e2 instanceof Address)
				{
					Address a2 = (Address) e2;
					return compare(p1, a2, ascending);
				}
			}
			else if (e1 instanceof LinkPersonAddress)
			{
				LinkPersonAddress l1 = (LinkPersonAddress) e1;
				if (e2 instanceof Person)
				{
					Person p2 = (Person) e2;
					return compare(l1.getPerson(), p2, ascending);
				}
				else if (e2 instanceof LinkPersonAddress)
				{
					LinkPersonAddress l2 = (LinkPersonAddress) e2;
					return compare(l1.getPerson(), l2.getPerson(), ascending);
				}
				else if (e2 instanceof Address)
				{
					Address a2 = (Address) e2;
					return compare(l1.getPerson(), a2, ascending);
				}
			}
			else if (e1 instanceof Address)
			{
				Address a1 = (Address) e1;
				if (e2 instanceof Person)
				{
					Person p2 = (Person) e2;
					return compare(a1, p2, ascending);
				}
				else if (e2 instanceof LinkPersonAddress)
				{
					LinkPersonAddress l2 = (LinkPersonAddress) e2;
					return compare(a1, l2.getPerson(), ascending);
				}
				else if (e2 instanceof Address)
				{
					Address a2 = (Address) e2;
					return compare(a1, a2, ascending);
				}
			}
			return 0;
		}

		public int compare(final Address a1, final Address a2, boolean ascending)
		{
			switch (this)
			{
				case CODE:
				{
					if (ascending)
						return this.compare(a1.getId(), a2.getId());
					else
						return this.compare(a2.getId(), a1.getId());
				}
				case NAME:
				{
					return 0;
				}
				case LASTNAME:
				{
					return 0;
				}
				case FIRSTNAME:
				{
					return 0;
				}
				case ADDRESS_ID:
				{
					if (ascending)
						return this.compare(a1.getId(), a2.getId());
					else
						return this.compare(a2.getId(), a1.getId());
				}
				case ORGANISATION:
				{
					if (ascending)
						return this.compare(a1.getName(), a2.getName());
					else
						return this.compare(a2.getName(), a1.getName());
				}
				case ADDRESS:
				{
					if (ascending)
						return this.compare(a1.getAddress(), a2.getAddress());
					else
						return this.compare(a2.getAddress(), a1.getAddress());
				}
				case CITY:
				{
					if (ascending)
						return this.compare(AddressFormatter.getInstance().formatCityLine(a1), AddressFormatter
								.getInstance().formatCityLine(a2));
					else
						return this.compare(AddressFormatter.getInstance().formatCityLine(a2), AddressFormatter
								.getInstance().formatCityLine(a1));
				}
				case PHONE:
				{
					String phone1 = a1.getPhone();
					String phone2 = a2.getPhone();

					if (ascending)
						return this.compare(phone1, phone2);
					else
						return this.compare(phone2, phone1);
				}
				case MOBILE:
				{
					return 0;
				}
				case FAX:
				{
					String fax1 = a1.getFax();
					String fax2 = a2.getFax();

					if (ascending)
						return this.compare(fax1, fax2);
					else
						return this.compare(fax2, fax1);
				}
				case EMAIL:
				{
					String email1 = a1.getEmail();
					String email2 = a2.getEmail();

					if (ascending)
						return this.compare(email1, email2);
					else
						return this.compare(email2, email1);
				}
				case DOMAIN:
				{
					return 0;
				}
				default:
					return 0;
			}
		}

		private int compare(final Person person, final Address address, final boolean ascending)
		{
			switch (this)
			{
				case CODE:
				{
					if (ascending)
						return this.compare(person.getId(), Long.valueOf(0L));
					else
						return this.compare(Long.valueOf(0L), person.getId());
				}
				case NAME:
				{
					if (ascending)
						return this.compare(PersonFormatter.getInstance().formatLastnameFirstname(person), "");
					else
						return this.compare("", PersonFormatter.getInstance().formatLastnameFirstname(person));
				}
				case LASTNAME:
				{
					if (ascending)
						return this.compare(person.getLastname(), "");
					else
						return this.compare("", person.getLastname());
				}
				case FIRSTNAME:
				{
					if (ascending)
						return this.compare(person.getFirstname(), "");
					else
						return this.compare("", person.getFirstname());
				}
				case ADDRESS_ID:
				{
					LinkPersonAddress link = person.getDefaultLink();
					if (ascending)
						return this.compare(link.getAddress().getId(), address.getId());
					else
						return this.compare(address.getId(), link.getAddress().getId());
				}
				case ORGANISATION:
				{
					LinkPersonAddress link = person.getDefaultLink();
					if (ascending)
						return this.compare(link.getAddress().getName(), address.getName());
					else
						return this.compare(address.getName(), link.getAddress().getName());
				}
				case ADDRESS:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String address1 = AddressFormatter.getInstance().formatAddressLine(link.getAddress());
					String address2 = address.getAddress();
					if (ascending)
						return this.compare(address1, address2);
					else
						return this.compare(address2, address1);
				}
				case CITY:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String address1 = AddressFormatter.getInstance().formatCityLine(link.getAddress());
					String address2 = AddressFormatter.getInstance().formatCityLine(address);
					if (ascending)
						return this.compare(address1, address2);
					else
						return this.compare(address2, address1);
				}
				case PHONE:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String phone = AddressFormatter.getInstance().formatPhone(link.getPerson().getCountry(),
							link.getPerson().getPhone());
					if (ascending)
						return this.compare(phone, "");
					else
						return this.compare("", phone);
				}
				case MOBILE:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String phone = AddressFormatter.getInstance().formatPhone(link.getPerson().getCountry(),
							link.getPhone());
					if (ascending)
						return this.compare(phone, "");
					else
						return this.compare("", phone);
				}
				case FAX:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String fax1 = AddressFormatter.getInstance().formatPhone(link.getAddress().getCountry(),
							link.getAddress().getFax());
					String fax2 = AddressFormatter.getInstance().formatPhone(address.getCountry(), address.getFax());
					if (ascending)
						return this.compare(fax1, fax2);
					else
						return this.compare(fax2, fax1);
				}
				case EMAIL:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String email1 = link.getEmail();
					if (email1.isEmpty())
					{
						email1 = link.getPerson().getEmail();
					}
					if (email1.isEmpty())
					{
						email1 = link.getAddress().getEmail();
					}
					String email2 = address.getEmail();
					if (ascending)
						return this.compare(email1, email2);
					else
						return this.compare(email2, email1);
				}
				case DOMAIN:
				{
					if (ascending)
						return this.compareDomain(person, null);
					else
						return this.compareDomain(null, person);
				}
				default:
					return 0;
			}
		}

		private int compare(final Address address, final Person person, final boolean ascending)
		{
			switch (this)
			{
				case CODE:
				{
					if (ascending)
						return this.compare(Long.valueOf(0L), person.getId());
					else
						return this.compare(person.getId(), Long.valueOf(0L));
				}
				case NAME:
				{
					if (ascending)
						return this.compare("", PersonFormatter.getInstance().formatLastnameFirstname(person));
					else
						return this.compare(PersonFormatter.getInstance().formatLastnameFirstname(person), "");
				}
				case LASTNAME:
				{
					if (ascending)
						return this.compare("", person.getLastname());
					else
						return this.compare(person.getLastname(), "");
				}
				case FIRSTNAME:
				{
					if (ascending)
						return this.compare("", person.getFirstname());
					else
						return this.compare(person.getFirstname(), "");
				}
				case ADDRESS_ID:
				{
					LinkPersonAddress link = person.getDefaultLink();
					if (ascending)
						return this.compare(address.getId(), link.getAddress().getId());
					else
						return this.compare(link.getAddress().getId(), address.getId());
				}
				case ORGANISATION:
				{
					LinkPersonAddress link = person.getDefaultLink();
					if (ascending)
						return this.compare(address.getName(), link.getAddress().getName());
					else
						return this.compare(link.getAddress().getName(), address.getName());
				}
				case ADDRESS:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String address1 = address.getAddress();
					String address2 = AddressFormatter.getInstance().formatAddressLine(link.getAddress());
					if (ascending)
						return this.compare(address1, address2);
					else
						return this.compare(address2, address1);
				}
				case CITY:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String address1 = AddressFormatter.getInstance().formatCityLine(address);
					String address2 = AddressFormatter.getInstance().formatCityLine(link.getAddress());
					if (ascending)
						return this.compare(address1, address2);
					else
						return this.compare(address2, address1);
				}
				case MOBILE:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String phone = AddressFormatter.getInstance().formatPhone(link.getPerson().getCountry(),
							link.getPhone());
					if (ascending)
						return this.compare("", phone);
					else
						return this.compare(phone, "");
				}
				case PHONE:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String phone = AddressFormatter.getInstance().formatPhone(link.getPerson().getCountry(),
							link.getPerson().getPhone());
					if (ascending)
						return this.compare("", phone);
					else
						return this.compare(phone, "");
				}
				case FAX:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String fax1 = AddressFormatter.getInstance().formatPhone(address.getCountry(), address.getFax());
					String fax2 = AddressFormatter.getInstance().formatPhone(link.getAddress().getCountry(),
							link.getAddress().getFax());
					if (ascending)
						return this.compare(fax1, fax2);
					else
						return this.compare(fax2, fax1);
				}
				case EMAIL:
				{
					LinkPersonAddress link = person.getDefaultLink();
					String email1 = address.getEmail();
					String email2 = link.getEmail();
					if (email2.isEmpty())
					{
						email2 = link.getPerson().getEmail();
					}
					if (email2.isEmpty())
					{
						email2 = link.getAddress().getEmail();
					}
					if (ascending)
						return this.compare(email1, email2);
					else
						return this.compare(email2, email1);
				}
				case DOMAIN:
				{
					if (ascending)
						return this.compareDomain(person, null);
					else
						return this.compareDomain(null, person);
				}
				default:
					return 0;
			}
		}

		private int compare(final Person p1, final Person p2, boolean ascending)
		{
			switch (this)
			{
				case CODE:
				{
					if (ascending)
						return this.compare(p1.getId(), p2.getId());
					else
						return this.compare(p2.getId(), p1.getId());
				}
				case NAME:
				{
					if (ascending)
						return this.compare(PersonFormatter.getInstance().formatLastnameFirstname(p1), PersonFormatter
								.getInstance().formatLastnameFirstname(p2));
					else
						return this.compare(PersonFormatter.getInstance().formatLastnameFirstname(p2), PersonFormatter
								.getInstance().formatLastnameFirstname(p1));
				}
				case LASTNAME:
				{
					if (ascending)
						return this.compare(p1.getLastname(), p2.getLastname());
					else
						return this.compare(p2.getLastname(), p1.getLastname());
				}
				case FIRSTNAME:
				{
					if (ascending)
						return this.compare(p1.getFirstname(), p2.getFirstname());
					else
						return this.compare(p2.getFirstname(), p1.getFirstname());
				}
				case ADDRESS_ID:
				{
					LinkPersonAddress link1 = p1.getDefaultLink();
					LinkPersonAddress link2 = p2.getDefaultLink();
					if (ascending)
						return compare(link1.getAddress().getId(), link2.getAddress().getId());
					else
						return compare(link2.getAddress().getId(), link1.getAddress().getId());
				}
				case ORGANISATION:
				{
					LinkPersonAddress link1 = p1.getDefaultLink();
					LinkPersonAddress link2 = p2.getDefaultLink();
					if (ascending)
						return this.compare(link1.getAddress().getName(), link2.getAddress().getName());
					else
						return this.compare(link2.getAddress().getName(), link1.getAddress().getName());
				}
				case ADDRESS:
				{
					LinkPersonAddress link1 = p1.getDefaultLink();
					LinkPersonAddress link2 = p2.getDefaultLink();
					String address1 = AddressFormatter.getInstance().formatAddressLine(link1.getAddress());
					String address2 = AddressFormatter.getInstance().formatAddressLine(link2.getAddress());
					if (ascending)
						return this.compare(address1, address2);
					else
						return this.compare(address2, address1);
				}
				case CITY:
				{
					LinkPersonAddress link1 = p1.getDefaultLink();
					LinkPersonAddress link2 = p2.getDefaultLink();
					String address1 = AddressFormatter.getInstance().formatCityLine(link1.getAddress());
					String address2 = AddressFormatter.getInstance().formatCityLine(link2.getAddress());
					if (ascending)
						return this.compare(address1, address2);
					else
						return this.compare(address2, address1);
				}
				case MOBILE:
				{
					LinkPersonAddress link1 = p1.getDefaultLink();
					LinkPersonAddress link2 = p2.getDefaultLink();
					String phone1 = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(p1.getCountry(),
							link1.getPhone());
					String phone2 = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(p2.getCountry(),
							link2.getPhone());
					if (ascending)
						return this.compare(phone1, phone2);
					else
						return this.compare(phone2, phone1);
				}
				case PHONE:
				{
					String phone1 = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(p1.getCountry(),
							p1.getPhone());
					String phone2 = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(p2.getCountry(),
							p2.getPhone());
					if (ascending)
						return this.compare(phone1, phone2);
					else
						return this.compare(phone2, phone1);
				}
				case FAX:
				{
					LinkPersonAddress link1 = p1.getDefaultLink();
					LinkPersonAddress link2 = p2.getDefaultLink();
					String fax1 = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(
							link1.getAddress().getCountry(), link1.getAddress().getFax());
					String fax2 = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(
							link2.getAddress().getCountry(), link2.getAddress().getFax());
					if (ascending)
						return this.compare(fax1, fax2);
					else
						return this.compare(fax2, fax1);
				}
				case EMAIL:
				{
					LinkPersonAddress link1 = p1.getDefaultLink();
					LinkPersonAddress link2 = p2.getDefaultLink();
					String email1 = p1.getEmail().isEmpty() ? (link1.getEmail().isEmpty() ? link1.getAddress()
							.getEmail() : link1.getEmail()) : p1.getEmail();
					String email2 = p2.getEmail().isEmpty() ? (link2.getEmail().isEmpty() ? link2.getAddress()
							.getEmail() : link2.getEmail()) : p2.getEmail();
					if (ascending)
						return this.compare(email1, email2);
					else
						return this.compare(email2, email1);
				}
				case DOMAIN:
				{
					if (ascending)
						return this.compareDomain(p1, p2);
					else
						return this.compareDomain(p2, p1);
				}
				default:
					return 0;
			}
		}

		private int compareDomain(final Person p1, final Person p2)
		{
			Domain domain1 = p1 == null ? null : p1.getDomain();
			Domain domain2 = p2 == null ? null : p2.getDomain();
			if (domain1 == null)
				if (domain2 == null)
					return 0;
				else
					return 1;
			else if (domain2 != null)
				return domain1.getCode().compareToIgnoreCase(domain2.getCode());
			else
				return -1;
		}

		private int compare(final Long l1, final Long l2)
		{
			return l1.compareTo(l2);
		}

		private int compare(final String s1, final String s2)
		{
			int c = s1.compareToIgnoreCase(s2);
			return c;
		}

	}
}
