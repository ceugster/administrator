package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class PersonSorter extends ViewerSorter
{
	private int activeColumn = 0;

	private boolean asc = true;

	private int compare(final Address a1, final Address a2)
	{
		switch (activeColumn)
		{
			case 0:
			{
				if (asc)
					return this.compare(a1.getId(), a2.getId());
				else
					return this.compare(a2.getId(), a1.getId());
			}
			case 1:
			{
				if (asc)
					return this.compare(a1.getName(), a2.getName());
				else
					return this.compare(a2.getName(), a1.getName());
			}
			case 2:
			{
				if (asc)
					return this.compare(a1.getAnotherLine(), a2.getAnotherLine());
				else
					return this.compare(a2.getAnotherLine(), a1.getAnotherLine());
			}
			case 3:
			{
				if (asc)
					return this.compare(a1.getId(), a2.getId());
				else
					return this.compare(a2.getId(), a1.getId());
			}
			case 4:
			{
				if (asc)
					return this.compare(a1.getName(), a2.getName());
				else
					return this.compare(a2.getName(), a1.getName());
			}
			case 5:
			{
				if (asc)
					return this.compare(a1.getAddress(), a2.getAddress());
				else
					return this.compare(a2.getAddress(), a1.getAddress());
			}
			case 6:
			{
				if (asc)
					return this.compare(AddressFormatter.getInstance().formatCityLine(a1), AddressFormatter
							.getInstance().formatCityLine(a2));
				else
					return this.compare(AddressFormatter.getInstance().formatCityLine(a2), AddressFormatter
							.getInstance().formatCityLine(a1));
			}
			case 7:
			{
				return 0;
			}
			case 8:
			{
				String phone1 = a1.getPhone();
				String phone2 = a2.getPhone();

				if (asc)
					return this.compare(phone1, phone2);
				else
					return this.compare(phone2, phone1);
			}
			case 9:
			{
				String email1 = a1.getEmail();
				String email2 = a2.getEmail();

				if (asc)
					return this.compare(email1, email2);
				else
					return this.compare(email2, email1);
			}
			case 10:
			{
				return 0;
			}
			default:
				return 0;
		}
	}

	private int compare(final boolean asc, final Person p1, final Address a2)
	{
		switch (activeColumn)
		{
			case 0:
			{
				if (asc)
					return this.compare(p1.getId(), a2.getId());
				else
					return this.compare(a2.getId(), p1.getId());
			}
			case 1:
			{
				if (asc)
					return this.compare(p1.getLastname(), a2.getName());
				else
					return this.compare(a2.getName(), p1.getLastname());
			}
			case 2:
			{
				if (asc)
					return this.compare(p1.getFirstname(), a2.getAnotherLine());
				else
					return this.compare(a2.getAnotherLine(), p1.getFirstname());
			}
			case 3:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();

				if (asc)
					return this.compare(link1.getAddress().getId(), a2.getId());
				else
					return this.compare(a2.getId(), link1.getAddress().getId());
			}
			case 4:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();

				if (asc)
					return this.compare(link1.getAddress().getName(), a2.getName());
				else
					return this.compare(a2.getName(), link1.getAddress().getName());
			}
			case 5:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();

				if (asc)
					return this.compare(link1.getAddress().getAddress(), a2.getAddress());
				else
					return this.compare(a2.getAddress(), link1.getAddress().getAddress());
			}
			case 6:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();
				String city1 = link1 == null ? "" : AddressFormatter.getInstance().formatCityLine(link1.getAddress());

				if (asc)
					return this.compare(city1, AddressFormatter.getInstance().formatCityLine(a2));
				else
					return this.compare(AddressFormatter.getInstance().formatCityLine(a2), city1);
			}
			case 7:
			{
				if (asc)
					return this.compare(p1.getPhone(), "");
				else
					return this.compare("", p1.getPhone());
			}
			case 8:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();

				String phone1 = link1.getPhone().isEmpty() ? link1.getAddress().getPhone() : link1.getPhone();
				String phone2 = a2.getPhone();

				if (asc)
					return this.compare(phone1, phone2);
				else
					return this.compare(phone2, phone1);
			}
			case 9:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();

				String email1 = link1.getEmail().isEmpty() ? link1.getAddress().getEmail() : link1.getEmail();
				String email2 = a2.getEmail();

				if (asc)
					return this.compare(email1, email2);
				else
					return this.compare(email2, email1);
			}
			case 10:
			{
				if (asc)
					return this.compareDomain(p1, null);
				else
					return this.compareDomain(null, p1);
			}
			default:
				return 0;
		}
	}

	private int compare(final Long l1, final Long l2)
	{
		return l1.compareTo(l2);
	}

	private int compare(final Person p1, final Person p2)
	{
		switch (activeColumn)
		{
			case 0:
			{
				if (this.asc)
					return this.compare(p1.getId(), p2.getId());
				else
					return this.compare(p2.getId(), p1.getId());
			}
			case 1:
			{
				if (this.asc)
					return this.compare(p1.getLastname(), p2.getLastname());
				else
					return this.compare(p2.getLastname(), p1.getLastname());
			}
			case 2:
			{
				if (this.asc)
					return this.compare(p1.getFirstname(), p2.getFirstname());
				else
					return this.compare(p2.getFirstname(), p1.getFirstname());
			}
			case 3:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();
				LinkPersonAddress link2 = p2.getDefaultLink();

				if (this.asc)
					return link1.getAddress().getId().compareTo(link2.getAddress().getId());
				else
					return link2.getAddress().getId().compareTo(link1.getAddress().getId());
			}
			case 4:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();
				LinkPersonAddress link2 = p2.getDefaultLink();

				if (this.asc)
					return this.compareOrganization(link1.getAddress(), link2.getAddress());
				else
					return this.compareOrganization(link2.getAddress(), link1.getAddress());
			}
			case 5:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();
				LinkPersonAddress link2 = p2.getDefaultLink();

				if (this.asc)
					return this.compareAddress(link1, link2);
				else
					return this.compareAddress(link2, link1);
			}
			case 6:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();
				LinkPersonAddress link2 = p2.getDefaultLink();

				String city1 = link1 == null ? "" : AddressFormatter.getInstance().formatCityLine(link1.getAddress());
				String city2 = link2 == null ? "" : AddressFormatter.getInstance().formatCityLine(link2.getAddress());
				if (this.asc)
					return this.compare(city1, city2);
				else
					return this.compare(city2, city1);
			}
			case 7:
			{
				if (this.asc)
					return this.compare(p1.getPhone(), p2.getPhone());
				else
					return this.compare(p2.getPhone(), p1.getPhone());
			}
			case 8:
			{
				LinkPersonAddress link1 = p1.getDefaultLink();
				LinkPersonAddress link2 = p2.getDefaultLink();

				String phone1 = link1 == null ? "" : link1.getPhone().isEmpty() ? link1.getAddress().getPhone() : link1
						.getPhone();
				String phone2 = link2 == null ? "" : link2.getPhone().isEmpty() ? link2.getAddress().getPhone() : link2
						.getPhone();

				if (this.asc)
					return this.compare(phone1, phone2);
				else
					return this.compare(phone2, phone1);
			}
			case 9:
			{

				LinkPersonAddress link1 = p1.getDefaultLink();
				LinkPersonAddress link2 = p2.getDefaultLink();

				String email1 = p1.getEmail().isEmpty() ? (link1.getEmail().isEmpty() ? link1.getAddress().getEmail()
						: link1.getEmail()) : p1.getEmail();
				String email2 = p2.getEmail().isEmpty() ? (link2.getEmail().isEmpty() ? link2.getAddress().getEmail()
						: link2.getEmail()) : p2.getEmail();

				if (this.asc)
					return this.compare(email1, email2);
				else
					return this.compare(email2, email1);
			}
			case 10:
			{
				if (this.asc)
					return this.compareDomain(p1, p2);
				else
					return this.compareDomain(p2, p1);
			}
			default:
				return 0;
		}
	}

	private int compare(final String s1, final String s2)
	{
		int c = s1.compareTo(s2);
		return c;
	}

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		if (e1 instanceof Person)
		{
			Person p1 = (Person) e1;
			if (e2 instanceof Person)
			{
				Person p2 = (Person) e2;
				return compare(p1, p2);
			}
			else if (e2 instanceof LinkPersonAddress)
			{
				LinkPersonAddress l2 = (LinkPersonAddress) e2;
				return compare(p1, l2.getPerson());
			}
			else if (e2 instanceof Address)
			{
				Address a2 = (Address) e2;
				return compare(asc, p1, a2);
			}
		}
		else if (e1 instanceof LinkPersonAddress)
		{
			LinkPersonAddress l1 = (LinkPersonAddress) e1;
			if (e2 instanceof Person)
			{
				Person p2 = (Person) e2;
				return compare(l1.getPerson(), p2);
			}
			else if (e2 instanceof LinkPersonAddress)
			{
				LinkPersonAddress l2 = (LinkPersonAddress) e2;
				return compare(l1.getPerson(), l2.getPerson());
			}
			else if (e2 instanceof Address)
			{
				Address a2 = (Address) e2;
				return compare(asc, l1.getPerson(), a2);
			}
		}
		else if (e1 instanceof Address)
		{
			Address a1 = (Address) e1;
			if (e2 instanceof Person)
			{
				Person p2 = (Person) e2;
				return compare(!asc, p2, a1);
			}
			else if (e2 instanceof LinkPersonAddress)
			{
				LinkPersonAddress l2 = (LinkPersonAddress) e2;
				return compare(!asc, l2.getPerson(), a1);
			}
			else if (e2 instanceof Address)
			{
				Address a2 = (Address) e2;
				return compare(a1, a2);
			}
		}
		return 0;
	}

	private int compareAddress(final LinkPersonAddress link1, final LinkPersonAddress link2)
	{
		String value1 = AddressFormatter.getInstance().formatAddressLine(link1.getAddress());
		String value2 = AddressFormatter.getInstance().formatAddressLine(link2.getAddress());

		return value1.compareTo(value2);
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
			return domain1.getCode().compareTo(domain2.getCode());
		else
			return -1;
	}

	private int compareOrganization(final Address a1, final Address a2)
	{
		int compare = this.compare(a1.getName(), a2.getName());
		if (compare == 0)
		{
			compare = this.compare(a1.getName(), a2.getName());
		}
		return compare;
	}

	public int getCurrentColumn()
	{
		return activeColumn;
	}

	public boolean isAscending()
	{
		return asc;
	}

	public void setAscending(final boolean asc)
	{
		this.asc = asc;
	}

	// private int compareEmail(LinkPersonAddress link1, LinkPersonAddress
	// link2)
	// {
	// return
	// link1.getPerson().getEmail().compareTo(link2.getPerson().getEmail());
	// }

	public void setCurrentColumn(final int column)
	{
		if (column == this.activeColumn)
			this.asc = !this.asc;
		else
			this.activeColumn = column;
	}

}
