package ch.eugster.events.persistence.model;

import java.util.List;

public interface AddressGroupMemberCapable extends IEntity
{
	void addAddressGroupMember(final AddressGroupMember addressGroupMember);

	void removeAddressGroupMember(final AddressGroupMember addressGroupMember);

	List<AddressGroupMember> getAddressGroupMembers();
}
