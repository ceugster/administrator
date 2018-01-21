package ch.eugster.events.addressgroup;

import ch.eugster.events.persistence.model.AddressGroup;

public interface AddressGroupMemberSelector 
{
	boolean isChecked(AddressGroup addressGroup);
}
