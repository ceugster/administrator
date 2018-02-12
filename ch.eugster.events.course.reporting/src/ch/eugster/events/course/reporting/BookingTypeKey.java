package ch.eugster.events.course.reporting;

import ch.eugster.events.documents.maps.DataMapKey;

public class BookingTypeKey implements DataMapKey, Comparable<BookingTypeKey>
{
	private String key;
	
	private String name;
	
	private String description;
	
	public BookingTypeKey(String key, String name, String description)
	{
		this.key = key;
		this.name = name;
		this.description = description;
	}
	
	@Override
	public Class<?> getType()
	{
		return String.class;
	}

	@Override
	public String getDescription() 
	{
		return this.description;
	}

	@Override
	public String getKey() 
	{
		return this.key;
	}

	@Override
	public String getName() 
	{
		return this.name;
	}

	public boolean equals(BookingTypeKey other) 
	{
		if (other instanceof BookingTypeKey)
		{
			return ((BookingTypeKey) other).getKey().equals(this.getKey());
		}
		return true;
	}

	@Override
	public int compareTo(BookingTypeKey other)
	{
		return this.getKey().compareTo(other.getKey());
	}
}
