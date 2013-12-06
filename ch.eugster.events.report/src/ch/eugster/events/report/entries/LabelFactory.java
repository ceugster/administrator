package ch.eugster.events.report.entries;

import java.util.HashMap;
import java.util.Map;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;

public class LabelFactory
{
	private Map<String, LabelEntry> entries = new HashMap<String, LabelEntry>();

	public boolean addEntry(final Address address)
	{
		boolean added = false;
		LabelEntry entry = null;
		if (!entries.containsKey("A" + address.getId().toString()))
		{
			entry = new LabelEntry(address);
			entries.put(entry.getId(), entry);
			added = true;
		}
		return added;
	}

	public boolean addEntry(final LinkPersonAddress link)
	{
		boolean added = false;
		LabelEntry entry = null;
		if (!entries.containsKey("L" + link.getId().toString()))
		{
			entry = new LabelEntry(link);
			entries.put(entry.getId(), entry);
			added = true;
		}
		return added;
	}

	public void clear()
	{
		entries.clear();
	}

	public LabelEntry[] getEntries()
	{
		return entries.values().toArray(new LabelEntry[0]);
	}

	public int size()
	{
		return entries.size();
	}
}
