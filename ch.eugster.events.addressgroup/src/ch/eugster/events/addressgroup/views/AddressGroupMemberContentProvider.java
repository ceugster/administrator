/*
 * Created on 17.12.2008 To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.addressgroup.views;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;

public class AddressGroupMemberContentProvider implements IStructuredContentProvider
{
	public AddressGroupMemberContentProvider(final AddressGroupMemberView view)
	{
	}

	private void addMembers(final AddressGroup addressGroup, final Collection<AddressGroupMember> members)
	{
		Collection<AddressGroupMember> groupMembers = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember groupMember : groupMembers)
		{
			if (!members.contains(groupMember) && !groupMember.isDeleted())
			{
				members.add(groupMember);
			}
		}
		// Collection<AddressGroupLink> children = addressGroup.getChildren();
		// for (AddressGroupLink child : children)
		// {
		// if (!child.isDeleted() && !child.getChild().isDeleted())
		// {
		// addMembers(child.getChild(), members);
		// }
		// }
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object object)
	{
		Collection<AddressGroupMember> members = new ArrayList<AddressGroupMember>();
		if (object instanceof AddressGroup)
		{
			AddressGroup group = (AddressGroup) object;
			addMembers(group, members);
		}
		return members.toArray(new AddressGroupMember[0]);
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
