package ch.eugster.events.addressgroup.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.ui.helpers.EmailHelper;

public class RecipientListFactory
{
	private static Map<String, Recipient> recipients = new HashMap<String, Recipient>();

	private static Map<Long, AddressGroup> addressGroups = new HashMap<Long, AddressGroup>();

	public static void addAddressGroup(final AddressGroup addressGroup)
	{
		if (addressGroups.get(addressGroup.getId()) == null)
		{
			addressGroups.put(addressGroup.getId(), addressGroup);
		}
	}

	public static boolean addRecipient(final AddressGroupMember member)
	{
		boolean added = false;
		Recipient recipient = null;
		if (!recipients.containsKey(member.getId().toString()))
		{
			recipient = new Recipient(member);
			recipients.put(member.getId().toString(), recipient);
			added = true;
		}
		return added;
	}

	public static boolean addRecipientWithEmails(final AddressGroupMember member)
	{
		boolean added = false;
		Recipient recipient = null;
		if (member.isValidAddressMember())
		{
			if (!recipients.containsKey(member.getAddress().getEmail()))
			{
				recipient = new Recipient(member);
				recipients.put(member.getAddress().getEmail(), recipient);
				added = true;
			}
		}
		else if (member.isValidLinkMember())
		{
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getEmail()))
			{
				if (!recipients.containsKey(member.getLink().getEmail()))
				{
					recipient = new Recipient(member);
					recipients.put(member.getLink().getEmail(), recipient);
					added = true;
				}
			}
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getPerson().getEmail()))
			{
				if (!recipients.containsKey(member.getLink().getPerson().getEmail()))
				{
					recipient = new Recipient(member, member.getLink().getPerson());
					recipients.put(member.getLink().getPerson().getEmail(), recipient);
					added = true;
				}
			}
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getAddress().getEmail()))
			{
				if (!recipients.containsKey(member.getLink().getAddress().getEmail()))
				{
					recipient = new Recipient(member, member.getLink().getAddress());
					recipients.put(member.getLink().getAddress().getEmail(), recipient);
					added = true;
				}
			}
		}
		return added;
	}

	public static void clear()
	{
		recipients.clear();
		addressGroups.clear();
	}

	public static String getAddressGroupParameter()
	{
		StringBuilder builder = new StringBuilder("Adressgruppen: ");
		Collection<AddressGroup> addressGroups = RecipientListFactory.addressGroups.values();
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
		Collection<Recipient> recipients = RecipientListFactory.recipients.values();
		for (Recipient recipient : recipients)
		{
			if (!emails.contains(recipient.getEmail()))
			{
				emails.add(recipient.getEmail());
			}
		}
		return emails.toArray(new String[0]);
	}

	public static Recipient[] getRecipients()
	{
		return recipients.values().toArray(new Recipient[0]);
	}

	public static int size()
	{
		return recipients.size();
	}

	public static boolean isEmpty()
	{
		return recipients.size() == 0;
	}
}
