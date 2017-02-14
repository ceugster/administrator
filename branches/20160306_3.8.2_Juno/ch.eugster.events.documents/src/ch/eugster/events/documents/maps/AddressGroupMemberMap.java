package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.util.List;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class AddressGroupMemberMap extends AbstractDataMap<AddressGroupMember>
{
	protected AddressGroupMemberMap() {
		super();
	}

	public AddressGroupMemberMap(final AddressGroupMember member, boolean isGroup)
	{
//		if (member.isValidLinkMember() && member.getLink().getPerson().getLastname().equals("Amsler") && member.getLink().getPerson().getFirstname().equals("Ursula"))
//		{
//			System.out.println();
//		}
//		if (member.isValidAddressMember() && member.getAddress().getAddress().equals("Platz 10") && member.getAddress().getCity().equals("Herisau"))
//		{
//			System.out.println();
//		}
		isGroup = isGroup(member, isGroup);
		for (Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(member, isGroup));
		}
		this.setProperties(new AddressGroupMap(member.getAddressGroup()).getProperties());
		if (member.isValidAddressMember())
		{
//			if (member.getAddress().getValidLinks().size() == 1)
//			{
//				this.setProperties(new LinkMap(member.getAddress().getValidLinks().iterator().next()).getProperties());
//			}
//			else
//			{
				this.setProperties(new AddressMap(member.getAddress(), isGroup).getProperties());
//			}
		}
		else if (isGroup && member.isValidLinkMember() && member.getLink().getAddress().getValidLinks().size() > 1)
		{
			this.setProperties(new AddressMap(member.getLink().getAddress(), isGroup).getProperties());
		}
		else if (member.isValidLinkMember())
		{
			this.setProperties(new LinkMap(member.getLink()).getProperties());
		}
	}
	
	private boolean isGroup(AddressGroupMember member, boolean isGroup)
	{
		if (!isGroup) return false;
		if (member.isValidAddressMember())
		{
			return true;
		}
		List<AddressGroupMember> ms = member.getAddressGroup().getAddressGroupMembers();
		if (member.isValidLinkMember())
		{
			for (AddressGroupMember m : ms)
			{
				if (m.isValidAddressMember() && m.getAddress().getId().equals(member.getLink().getAddress().getId()))
				{
					return true;
				}
				else if (m.isValidLinkMember() && m.getLink().getAddress().getId().equals(member.getLink().getAddress().getId()))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}

	public String getId()
	{
		return this.getProperty(Key.ID.getKey());
	}

	protected void printReferences(Writer writer)
	{
		printHeader(writer, 2, "Referenzen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, "#link", "Link Person/Adresse");
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, "#address", "Adresse");
		endTableRow(writer);
		endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		ID, ANOTHER_LINE, SALUTATION, POLITE, MAILING_ADDRESS;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case ID:
				{
					return "Id";
				}
				case ANOTHER_LINE:
				{
					return "Zusatzzeile";
				}
				case SALUTATION:
				{
					return "Anrede";
				}
				case POLITE:
				{
					return "Briefanrede";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getKey()
		{
			switch (this)
			{
				case ID:
				{
					return "address_group_member_id";
				}
				case ANOTHER_LINE:
				{
					return "address_group_member_another_line";
				}
				case SALUTATION:
				{
					return "address_group_member_salutation";
				}
				case POLITE:
				{
					return "address_group_member_polite";
				}
				case MAILING_ADDRESS:
				{
					return "address_group_member_mailing_address";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getName()
		{
			switch (this)
			{
				case ID:
				{
					return "Id";
				}
//				case TYPE:
//				{
//					return "Typ";
//				}
				case ANOTHER_LINE:
				{
					return "Zusatzzeile";
				}
				case SALUTATION:
				{
					return "Anrede";
				}
				case POLITE:
				{
					return "Briefanrede";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final AddressGroupMember member, boolean isGroup)
		{
			switch (this)
			{
				case ID:
				{
					if (member.isValidAddressMember())
					{
						return "A" + member.getAddress().getId().toString();
					}
					else if (isGroup)
					{
						return "A" + member.getLink().getAddress().getId().toString();
					}
					else if (member.isValidLinkMember())
					{
						return "P" + member.getLink().getPerson().getId().toString();
					}
					else
					{
						return "";
					}
				}
				case ANOTHER_LINE:
				{
					String anotherLine = "";
					if (member.isValidAddressMember())
					{
						anotherLine = member.getAddress().getAnotherLine();
					}
					else if (member.isValidLinkMember())
					{
						anotherLine = member.getLink().getAddress().getAnotherLine();
					}
					return anotherLine;
				}
				case SALUTATION:
				{
					if (member.isValidAddressMember())
					{
						return member.getAddress().getSalutation() == null ? "" : member.getAddress().getSalutation()
									.getSalutation();
					}
					else if (member.isValidLinkMember())
					{
						if (isGroup && member.getLink().getAddress().getValidLinks().size() > 1)
						{
							return member.getLink().getAddress().getSalutation() == null ? "" : member.getLink().getAddress().getSalutation()
								.getSalutation();
						}
						else
						{
							Person person = member.getLink().getPerson();
							return person.getSex() == null ? "Fehler!" : person.getSex().getSalutation();
						}
					}
				}
				case POLITE:
				{
					String polite = null;
					if (member.isValidAddressMember())
					{
//						if (member.getAddress().getValidLinks().size() == 1)
//						{
//							Person person = member.getAddress().getValidLinks().iterator().next().getPerson();
//							polite = person.getSex() == null ? "Fehler!" : PersonFormatter.getInstance()
//									.replaceSalutationVariables(person, person.getSex().getForm(person.getForm()));
//						}
//						else
//						{
							AddressSalutation salutation = member.getAddress().getSalutation();
							polite = salutation == null ? "" : salutation.getPolite();
							if (polite.isEmpty())
							{
								polite = "Sehr geehrte Damen und Herren";
							}
//						}
					}
					else if (member.isValidLinkMember())
					{
						if (isGroup && member.getLink().getAddress().getValidLinks().size() > 1)
						{
							AddressSalutation salutation = member.getLink().getAddress().getSalutation();
							polite = salutation == null ? "" : salutation.getPolite();
							if (polite.isEmpty())
							{
								polite = "Sehr geehrte Damen und Herren";
							}
						}
						else
						{
							Person person = member.getLink().getPerson();
							polite = person.getSex() == null ? "Fehler!" : PersonFormatter.getInstance()
									.replaceSalutationVariables(person, person.getSex().getForm(person.getForm()));
						}
					}
					return polite;
				}
				case MAILING_ADDRESS:
				{
					if (member.isValidAddressMember())
					{
						if (member.getAddress().getValidLinks().size() == 1)
						{
							LinkPersonAddress link = member.getAddress().getValidLinks().iterator().next();
							return LinkPersonAddressFormatter.getInstance().getLabel(link);
						}
						else
						{
							return AddressFormatter.getInstance().formatAddressLabel(member.getAddress());
						}
					}
					else if (member.isValidLinkMember())
					{
						if (isGroup && member.getLink().getAddress().getValidLinks().size() > 1)
						{
							return AddressFormatter.getInstance().formatAddressLabel(member.getLink().getAddress());
						}
						else
						{
							LinkPersonAddress link = member.getLink();
							return LinkPersonAddressFormatter.getInstance().getLabel(link);
						}
					}
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}
}
