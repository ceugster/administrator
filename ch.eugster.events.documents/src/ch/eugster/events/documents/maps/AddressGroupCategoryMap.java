package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.AddressGroupCategory;

public class AddressGroupCategoryMap extends AbstractDataMap<AddressGroupCategory>
{
	protected AddressGroupCategoryMap() {
		super();
	}

	public AddressGroupCategoryMap(final AddressGroupCategory category)
	{
		for (Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(category));
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}

	@Override
	public int compareTo(DataMap<AddressGroupCategory> other) 
	{
		return 0;
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
					return "Code der Adressgruppenkategorie";
				}
				case NAME:
				{
					return "Bezeichnung der Adressgruppenkategorie";
				}
				case DESCRIPTION:
				{
					return "Beschreibung der Adressgruppekategorie";
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
					return "address_group_category_code";
				}
				case NAME:
				{
					return "address_group_category_name";
				}
				case DESCRIPTION:
				{
					return "address_group_category_description";
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

		public String getValue(final AddressGroupCategory category)
		{
			switch (this)
			{
				case CODE:
				{
					return category.getCode();
				}
				case NAME:
				{
					return category.getName();
				}
				case DESCRIPTION:
				{
					return category.getDescription();
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
}
