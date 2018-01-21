package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.Category;

public class CategoryMap extends AbstractDataMap<Category>
{
	protected CategoryMap() {
		super();
	}

	public CategoryMap(final Category category)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(category));
		}
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
					return "Code der Kategorie";
				}
				case NAME:
				{
					return "Bezeichnung der Kategorie";
				}
				case DESCRIPTION:
				{
					return "Beschreibung der Kategorie";
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
					return "category_code";
				}
				case NAME:
				{
					return "category_name";
				}
				case DESCRIPTION:
				{
					return "category_description";
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

		public String getValue(final Category category)
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
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
