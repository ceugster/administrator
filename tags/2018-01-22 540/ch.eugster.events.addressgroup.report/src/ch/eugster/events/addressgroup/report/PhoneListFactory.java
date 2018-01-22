package ch.eugster.events.addressgroup.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class PhoneListFactory
{
	private static Map<String, PhoneListEntry> entries = new HashMap<String, PhoneListEntry>();

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
		PhoneListEntry entry = null;
		if (member.isValidAddressMember())
		{
			if (!entries.containsKey("A" + member.getAddress().getId().toString()))
			{
				entry = new PhoneListEntry(member);
				entries.put("A" + member.getAddress().getId().toString(), entry);
				added = true;
			}
		}
		else if (member.isValidLinkMember())
		{
			if (!entries.containsKey("L" + member.getLink().getId().toString()))
			{
				entry = new PhoneListEntry(member);
				entries.put("L" + member.getLink().getId().toString(), entry);
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

	public static String getAddressGroupParameter()
	{
		StringBuilder builder = new StringBuilder("Adressgruppen: ");
		Collection<AddressGroup> addressGroups = PhoneListFactory.addressGroups.values();
		for (AddressGroup addressGroup : addressGroups)
		{
			if (!addressGroup.getCode().isEmpty())
			{
				builder = builder.append(addressGroup.getCode());
				if (!addressGroup.getName().isEmpty())
				{
					builder = builder.append(" - ");
				}
			}
			if (!addressGroup.getName().isEmpty())
			{
				builder = builder.append(addressGroup.getName());
				builder = builder.append("; ");
			}
		}
		return builder.toString();
	}

	public static String[] getEmails()
	{
		List<String> emails = new ArrayList<String>();
		Collection<PhoneListEntry> entries = PhoneListFactory.entries.values();
		for (PhoneListEntry entry : entries)
		{
			if (!emails.contains(entry.getEmail()))
			{
				emails.add(entry.getEmail());
			}
		}
		return emails.toArray(new String[0]);
	}

	public static PhoneListEntry[] getEntries()
	{
		return entries.values().toArray(new PhoneListEntry[0]);
	}

	public static int size()
	{
		return entries.size();
	}
}
