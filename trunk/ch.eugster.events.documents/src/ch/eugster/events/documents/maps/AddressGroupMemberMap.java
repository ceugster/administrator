package ch.eugster.events.documents.maps;

import java.io.Writer;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class AddressGroupMemberMap extends AbstractDataMap implements Comparable<AddressGroupMemberMap>
{
	protected AddressGroupMemberMap() {
		super();
	}

	public AddressGroupMemberMap(final AddressGroupMember member, boolean isGroup)
	{
		for (Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(member, isGroup));
		}
		this.setProperties(new AddressGroupMap(member.getAddressGroup()).getProperties());
		if (member.getLink() == null || (isGroup && member.getAddress().getValidLinks().size() > 1))
		{
			this.setProperties(new AddressMap(member.getAddress()).getProperties());
		}
		else
		{
			this.setProperties(new LinkMap(member.getLink()).getProperties());
		}
	}

	public String getId()
	{
		return this.getProperty(Key.TYPE.getKey()) + this.getProperty(Key.ID.getKey());
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
		ID, TYPE, ANOTHER_LINE, SALUTATION, POLITE, MAILING_ADDRESS;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case ID:
				{
					return "Id";
				}
				case TYPE:
				{
					return "Typ";
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
				case TYPE:
				{
					return "address_group_member_type";
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
				case TYPE:
				{
					return "Typ";
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

		public String getValue(final AddressGroupMember member, boolean isGroup)
		{
			switch (this)
			{
				case ID:
				{
					return member.getLink() == null || member.getLink().isDeleted()
							|| member.getLink().getPerson().isDeleted() || isGroup ? member.getAddress().getId()
							.toString() : member.getLink().getPerson().getId().toString();
				}
				case TYPE:
				{
					return member.getLink() == null || member.getLink().isDeleted()
							|| member.getLink().getPerson().isDeleted() || isGroup ? "A" : "P";
				}
				case ANOTHER_LINE:
				{
					String anotherLine = "";
					if (member.getLink() == null || member.getLink().isDeleted()
							|| member.getLink().getPerson().isDeleted() || isGroup)
					{
						anotherLine = member.getAddress().getAnotherLine();
					}
					else
					{
						anotherLine = member.getLink().getAddress().getAnotherLine();
					}
					return anotherLine;
				}
				case SALUTATION:
				{
					if (member.getLink() == null || member.getLink().isDeleted()
							|| member.getLink().getPerson().isDeleted() || isGroup)
					{
						return member.getAddress().getSalutation() == null ? "" : member.getAddress().getSalutation()
								.getSalutation();
					}
					else
					{
						Person person = member.getLink().getPerson();
						return person.getSex() == null ? "Fehler!" : person.getSex().getSalutation();
					}
				}
				case POLITE:
				{
					String polite = null;
					if (member.getLink() == null || member.getLink().isDeleted()
							|| member.getLink().getPerson().isDeleted() || isGroup)
					{
						AddressSalutation salutation = member.getAddress().getSalutation();
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
					return polite;
				}
				case MAILING_ADDRESS:
				{
					if (member.getLink() == null || member.getLink().isDeleted()
							|| member.getLink().getPerson().isDeleted() || isGroup)
					{
						return AddressFormatter.getInstance().formatAddressLabel(member.getAddress());
					}
					else
					{
						LinkPersonAddress link = member.getLink();
						return LinkPersonAddressFormatter.getInstance().getLabel(link);
					}
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

	@Override
	public int compareTo(AddressGroupMemberMap other)
	{
		return 0;
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}

}
