package ch.eugster.events.addressgroup;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;

public class AddressGroupFormatter
{
	private static AddressGroupFormatter formatter;

	public static AddressGroupFormatter getInstance()
	{
		if (formatter == null)
			formatter = new AddressGroupFormatter();

		return formatter;
	}

	public String formatAddressGroupCategoryTreeLabel(AddressGroupCategory category)
	{
		StringBuffer buffer = new StringBuffer();
		if (category.getCode().length() > 0)
		{
			buffer = buffer.append(category.getCode());
			if (category.getName().length() > 0)
				buffer = buffer.append(" - ");
		}
		if (category.getName().length() > 0)
		{
			buffer = buffer.append(category.getName());
		}
		if (buffer.length() == 0)
			buffer = buffer.append("Ohne Bezeichnung");

		return buffer.toString();
	}

	public String formatAddressGroupTreeLabel(AddressGroup addressGroup)
	{
		StringBuffer buffer = new StringBuffer();
		if (addressGroup.getCode().length() > 0)
		{
			buffer = buffer.append(addressGroup.getCode());
			if (addressGroup.getName().length() > 0)
				buffer = buffer.append(" - ");
		}
		if (addressGroup.getName().length() > 0)
		{
			buffer = buffer.append(addressGroup.getName());
		}
		if (buffer.length() == 0)
			buffer = buffer.append("Ohne Bezeichnung");

		return buffer.toString();
	}
}
