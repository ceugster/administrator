package ch.eugster.events.addressgroup.report;

import java.util.HashMap;
import java.util.Map;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class LabelFactory
{
	private static Map<String, LabelEntry> entries = new HashMap<String, LabelEntry>();

	private static Map<Long, AddressGroup> addressGroups = new HashMap<Long, AddressGroup>();

	public static void addAddressGroup(final AddressGroup addressGroup)
	{
		if (addressGroups.get(addressGroup.getId()) == null)
		{
			addressGroups.put(addressGroup.getId(), addressGroup);
		}
	}

	public static boolean addEntry(final AddressGroupMember member)
	{
		boolean added = false;
		LabelEntry entry = null;
		if (member.getLink() == null)
		{
			if (!entries.containsKey("A" + member.getAddress().getId().toString()))
			{
				entry = new LabelEntry(member);
				entries.put(entry.getId(), entry);
				added = true;
			}
		}
		else
		{
			if (!entries.containsKey("L" + member.getLink().getId().toString()))
			{
				entry = new LabelEntry(member);
				entries.put(entry.getId(), entry);
				added = true;
			}
		}
		return added;
	}

	public static void clear()
	{
		entries.clear();
		addressGroups.clear();
	}

	public static LabelEntry[] getEntries()
	{
		return entries.values().toArray(new LabelEntry[0]);
	}

	public static int size()
	{
		return entries.size();
	}
}
