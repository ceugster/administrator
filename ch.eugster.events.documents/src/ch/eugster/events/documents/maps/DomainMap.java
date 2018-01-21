package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.Domain;

public class DomainMap extends AbstractDataMap<Domain>
{
	protected DomainMap()
	{
		super();
	}
	
	public DomainMap(final Domain domain)
	{
		for (Key key : Key.values())
		{
			setProperty(key.getKey(), key.getValue(domain));
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
					return "Code der Domäne";
				}
				case NAME:
				{
					return "Bezeichnung der Domäne";
				}
				case DESCRIPTION:
				{
					return "Beschreibung der Domäne";
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
					return "domain_code";
				}
				case NAME:
				{
					return "domain_name";
				}
				case DESCRIPTION:
				{
					return "domain_description";
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

		public String getValue(final Domain domain)
		{
			switch (this)
			{
				case CODE:
				{
					return domain.getCode();
				}
				case NAME:
				{
					return domain.getName();
				}
				case DESCRIPTION:
				{
					return domain.getDescription();
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
