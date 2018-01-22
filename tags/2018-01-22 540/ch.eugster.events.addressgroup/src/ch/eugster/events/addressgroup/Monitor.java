package ch.eugster.events.addressgroup;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class Monitor
{
	public AddressGroupMember addressGroupMember = null;

	public boolean checked = false;

	public AddressGroup addressGroup = null;

	public Monitor(final AddressGroup addressGroup, boolean checked)
	{
		this.addressGroup = addressGroup;
		this.checked = checked;
	}

	public Monitor(final AddressGroupMember addressGroupMember, boolean checked)
	{
		this.addressGroupMember = addressGroupMember;
		this.addressGroup = addressGroupMember.getAddressGroup();
		this.checked = checked;
	}
	
	public boolean isValid()
	{
		return this.addressGroupMember != null && !this.addressGroupMember.isDeleted();
				
	}
}

