package ch.eugster.events.documents.maps;

import java.io.Writer;

import ch.eugster.events.persistence.model.AddressGroup;

public class AddressGroupMap extends AbstractDataMap<AddressGroup>
{
	protected AddressGroupMap() {
		super();
	}

	public AddressGroupMap(final AddressGroup addressGroup)
	{
		for (Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(addressGroup));
		}
		this.setProperties(new AddressGroupCategoryMap(addressGroup.getAddressGroupCategory()).getProperties());
	}

	protected void printReferences(Writer writer)
	{
		printHeader(writer, 2, "Referenzen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, "#addressGroupCategory", "Kategorie");
		endTableRow(writer);
		endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		CODE, NAME, DESCRIPTION;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case CODE:
				{
					return "Code der Adressgruppe";
				}
				case NAME:
				{
					return "Bezeichnung der Adressgruppe";
				}
				case DESCRIPTION:
				{
					return "Beschreibung der Adressgruppe";
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
				case CODE:
				{
					return "address_group_code";
				}
				case NAME:
				{
					return "address_group_name";
				}
				case DESCRIPTION:
				{
					return "address_group_description";
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
				case CODE:
				{
					return "Code";
				}
				case NAME:
				{
					return "Bezeichnung";
				}
				case DESCRIPTION:
				{
					return "Beschreibung";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final AddressGroup addressGroup)
		{
			switch (this)
			{
				case CODE:
				{
					return addressGroup.getCode();
				}
				case NAME:
				{
					return addressGroup.getName();
				}
				case DESCRIPTION:
				{
					return addressGroup.getDescription();
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
