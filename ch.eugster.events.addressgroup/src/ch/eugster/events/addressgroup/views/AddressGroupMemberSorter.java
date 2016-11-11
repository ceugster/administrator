package ch.eugster.events.addressgroup.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class AddressGroupMemberSorter extends ViewerSorter
{
	private int column = 0;

	private boolean ascending = true;

	public AddressGroupMemberSorter(final int column, final boolean ascending)
	{
		this.column = column;
		this.ascending = ascending;
	}

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		AddressGroupMember member1 = (AddressGroupMember) e1;
		AddressGroupMember member2 = (AddressGroupMember) e2;

		String v1 = "";
		String v2 = "";

		switch (this.column)
		{
			case 0:
			{
				if (member1.isValidAddressMember() && member2.isValidAddressMember())
				{
					v1 = AddressFormatter.getInstance().formatId(member1.getAddress());
					v2 = AddressFormatter.getInstance().formatId(member2.getAddress());
				}
				else if (member1.isValidAddressMember() && member2.isValidLinkMember())
				{
					v1 = AddressFormatter.getInstance().formatId(member1.getAddress());
					v2 = PersonFormatter.getInstance().formatId(member2.getLink().getPerson());
				}
				else if (member1.isValidLinkMember() && member2.isValidAddressMember())
				{
					v1 = PersonFormatter.getInstance().formatId(member1.getLink().getPerson());
					v2 = AddressFormatter.getInstance().formatId(member2.getAddress());
				}
				else
				{
					v1 = PersonFormatter.getInstance().formatId(member1.getLink().getPerson());
					v2 = PersonFormatter.getInstance().formatId(member2.getLink().getPerson());
				}
				break;

			}
			case 1:
			{
				if (member1.isValidAddressMember() && member2.isValidAddressMember())
				{
					v1 = member1.getAddress().getName();
					v2 = member2.getAddress().getName();
				}
				else if (member1.isValidAddressMember() && member2.isValidLinkMember())
				{
					v1 = member1.getAddress().getName();
					v2 = PersonFormatter.getInstance().formatLastnameFirstname(member2.getLink().getPerson());
				}
				else if (member1.isValidLinkMember() && member2.isValidAddressMember())
				{
					v1 = PersonFormatter.getInstance().formatLastnameFirstname(member1.getLink().getPerson());
					v2 = member2.getAddress().getName();
				}
				else
				{
					v1 = PersonFormatter.getInstance().formatLastnameFirstname(member1.getLink().getPerson());
					v2 = PersonFormatter.getInstance().formatLastnameFirstname(member2.getLink().getPerson());
				}
				break;
			}
			case 2:
			{
				if (member1.isValidAddressMember() && member2.isValidAddressMember())
				{
					v1 = member1.getAddress().getAddress();
					v2 = member2.getAddress().getAddress();
				}
				else if (member1.isValidAddressMember() && member2.isValidLinkMember())
				{
					v1 = member1.getAddress().getAddress();
					v2 = member2.getLink().getAddress().getAddress();
				}
				else if (member1.isValidLinkMember() && member2.isValidAddressMember())
				{
					v1 = member1.getLink().getAddress().getAddress();
					v2 = member2.getAddress().getAddress();
				}
				else
				{
					v1 = member1.getLink().getAddress().getAddress();
					v2 = member2.getLink().getAddress().getAddress();
				}
				break;
			}
			case 3:
			{
				if (member1.isValidAddressMember() && member2.isValidLinkMember())
				{
					v1 = member1.getAddress().getCity();
					v2 = member2.getAddress().getCity();
				}
				else if (member1.isValidAddressMember() && member2.isValidLinkMember())
				{
					v1 = member1.getAddress().getCity();
					v2 = member2.getLink().getAddress().getCity();
				}
				else if (member1.isValidLinkMember() && member2.isValidAddressMember())
				{
					v1 = member1.getLink().getAddress().getCity();
					v2 = member2.getAddress().getCity();
				}
				else
				{
					v1 = member1.getLink().getAddress().getCity();
					v2 = member2.getLink().getAddress().getCity();
				}
				break;
			}
			case 4:
			{
				if (member1.isValidAddressMember() && member2.isValidAddressMember())
				{
					v1 = member1.getAddress().getEmail();
					v2 = member2.getAddress().getEmail();
				}
				else if (member1.isValidAddressMember() && member2.isValidLinkMember())
				{
					v1 = member1.getAddress().getEmail();
					if (member2.getLink().getPerson().getEmail().isEmpty())
					{
						v2 = member2.getLink().getPerson().getEmail();
					}
					else
					{
						v2 = member2.getLink().getEmail();
					}
				}
				else if (member1.isValidLinkMember() && member2.isValidAddressMember())
				{
					if (member1.getLink().getPerson().getEmail().isEmpty())
					{
						v1 = member1.getLink().getPerson().getEmail();
					}
					else
					{
						v1 = member1.getLink().getEmail();
					}
					v2 = member2.getAddress().getEmail();
				}
				else
				{
					if (member1.getLink().getPerson().getEmail().isEmpty())
					{
						v1 = member1.getLink().getPerson().getEmail();
					}
					else
					{
						v1 = member1.getLink().getEmail();
					}
					if (member2.getLink().getPerson().getEmail().isEmpty())
					{
						v2 = member2.getLink().getPerson().getEmail();
					}
					else
					{
						v2 = member2.getLink().getEmail();
					}
				}
				break;
			}
		}

		if (this.ascending)
			return v1.compareToIgnoreCase(v2);
		else
			return v2.compareToIgnoreCase(v1);
	}

	public boolean getAscending()
	{
		return this.ascending;
	}

	public int getColumn()
	{
		return this.column;
	}

	public void setAscending(final boolean ascending)
	{
		this.ascending = ascending;
	}

	public void setColumn(final int column)
	{
		this.column = column;
	}
}
