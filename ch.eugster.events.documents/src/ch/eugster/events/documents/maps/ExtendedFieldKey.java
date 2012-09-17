package ch.eugster.events.documents.maps;

import ch.eugster.events.persistence.model.FieldExtension;

public class ExtendedFieldKey implements DataMapKey
{
	private String key;

	private String name;

	private String description;

	public ExtendedFieldKey(final FieldExtension fieldExtension)
	{
		this.key = fieldExtension.getTarget().key() + fieldExtension.getLabel().toLowerCase();
		this.name = fieldExtension.getLabel();
		this.description = fieldExtension.getLabel();
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public String getName()
	{
		return name;
	}

}
