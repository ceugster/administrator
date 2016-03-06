package ch.eugster.events.addressgroup.dnd;

import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.Activator;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.dnd.EntityTransfer;

public class AddressGroupViewerDropAdapter extends ViewerDropAdapter
{
	public AddressGroupViewerDropAdapter(final Viewer viewer)
	{
		super(viewer);
	}

	private boolean applyFilter(final AbstractEntity entity)
	{
		return !entity.isDeleted();
	}

	private AddressGroup getTarget()
	{
		StructuredSelection ssel = (StructuredSelection) this.getViewer().getSelection();
		if (ssel.getFirstElement() instanceof AddressGroup)
		{
			return (AddressGroup) ssel.getFirstElement();
		}
		else if (this.getViewer().getInput() instanceof AddressGroup)
		{
			return (AddressGroup) this.getViewer().getInput();
		}
		return null;
	}

	private boolean insertAddress(final AddressGroup addressGroup, final Address address)
	{
		if (this.applyFilter(address))
		{
			AddressGroupMember addressGroupMember = AddressGroupMember.newInstance(addressGroup, address);
			return this.insertAddressGroupMember(addressGroup, addressGroupMember);
		}
		return false;
	}

	// private boolean insert(final AddressGroup parent, final AddressGroup
	// child)
	// {
	// boolean update = false;
	// if (this.applyFilter(child))
	// {
	// for (AddressGroupLink link : parent.getChildren())
	// {
	// if (link.getChild().getId().equals(child.getId()))
	// {
	// return false;
	// }
	// }
	// AddressGroupLink link = AddressGroupLink.newInstance(parent, child);
	// parent.addChild(link);
	// update = true;
	// }
	// return update;
	// }

	private boolean insertAddressGroupMember(final AddressGroup addressGroup, final AddressGroupMember newMember)
	{
		AddressGroupMember[] members = addressGroup.getAddressGroupMembers().toArray(new AddressGroupMember[0]);
		for (AddressGroupMember member : members)
		{
			if (newMember.getLink() == null || newMember.getLink().isDeleted()
					|| newMember.getLink().getPerson().isDeleted())
			{
				if (newMember.getAddress() != null)
				{
					if (member.getAddress() != null
							&& member.getAddress().getId().equals(newMember.getAddress().getId()))
					{
						if (member.isDeleted())
						{
							member.setDeleted(false);
							return true;
						}
						return false;
					}
				}
			}
			else
			{
				if (member.getLink() != null && !member.getLink().isDeleted()
						&& !member.getLink().getPerson().isDeleted())
				{
					if (member.getLink().getId().equals(newMember.getLink().getId()))
					{
						if (member.isDeleted())
						{
							member.setDeleted(false);
							return true;
						}
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean insertBooking(final AddressGroup addressGroup, final Booking booking)
	{
		boolean update = false;
		if (this.applyFilter(booking))
		{
			List<Participant> participants = booking.getParticipants();
			{
				for (Participant participant : participants)
				{
					if (this.insertParticipant(addressGroup, participant) && !update)
						update = true;
				}
			}
		}
		return update;
	}

	private boolean insertCourse(final AddressGroup group, final Course course)
	{
		boolean update = false;
		if (this.applyFilter(course))
		{
			List<Booking> bookings = course.getBookings();
			{
				for (Booking booking : bookings)
				{
					if (this.insertBooking(group, booking) && !update)
						update = true;
				}
			}
		}
		return update;
	}

	private boolean insertLinkPersonAddress(final AddressGroup addressGroup, final LinkPersonAddress link)
	{
		if (this.applyFilter(link))
		{
			AddressGroupMember addressGroupMember = AddressGroupMember.newInstance(addressGroup, link);
			return this.insertAddressGroupMember(addressGroup, addressGroupMember);
		}
		return false;
	}

	private boolean insertParticipant(final AddressGroup group, final Participant participant)
	{
		if (this.applyFilter(participant))
		{
			LinkPersonAddress link = participant.getLink();
			return this.insertLinkPersonAddress(group, link);
		}
		return false;
	}

	private boolean insertSeason(final AddressGroup group, final Season season)
	{
		boolean update = false;
		if (this.applyFilter(season))
		{
			List<Course> courses = season.getCourses();
			{
				for (Course course : courses)
				{
					if (this.insertCourse(group, course) && !update)
						update = true;
				}
			}
		}
		return update;
	}

	@Override
	public boolean performDrop(final Object data)
	{
		boolean updateGroup = false;
		AddressGroup target = getTarget();
		if (target instanceof AddressGroup)
		{
			if (data instanceof AbstractEntity[])
			{
				AbstractEntity[] entities = (AbstractEntity[]) data;
				for (AbstractEntity entity : entities)
				{
					if (entity instanceof LinkPersonAddress)
					{
						boolean doUpdate = this.insertLinkPersonAddress(target, (LinkPersonAddress) entity);
						if (!updateGroup)
						{
							updateGroup = doUpdate;
						}
					}
					else if (entity instanceof Address)
					{
						boolean doUpdate = this.insertAddress(target, (Address) entity);
						if (!updateGroup)
						{
							updateGroup = doUpdate;
						}
					}
					else if (entity instanceof Season)
					{
						boolean doUpdate = this.insertSeason(target, (Season) entity);
						if (!updateGroup)
						{
							updateGroup = doUpdate;
						}
					}
					else if (entity instanceof Course)
					{
						boolean doUpdate = this.insertCourse(target, (Course) entity);
						if (!updateGroup)
						{
							updateGroup = doUpdate;
						}
					}
					else if (entity instanceof Booking)
					{
						boolean doUpdate = this.insertBooking(target, (Booking) entity);
						if (!updateGroup)
						{
							updateGroup = doUpdate;
						}
					}
					else if (entity instanceof Participant)
					{
						boolean doUpdate = this.insertParticipant(target, (Participant) entity);
						if (!updateGroup)
						{
							updateGroup = doUpdate;
						}
					}
					// else if (entity instanceof AddressGroup)
					// {
					// boolean doUpdate = this.insert(target, (AddressGroup)
					// entity);
					// if (!updateGroup)
					// {
					// updateGroup = doUpdate;
					// }
					// }
					else if (entity instanceof AddressGroupMember)
					{
						boolean doUpdate = this.insertAddressGroupMember(target, (AddressGroupMember) entity);
						if (!updateGroup)
						{
							updateGroup = doUpdate;
						}
					}
				}
			}

			if (updateGroup)
			{
				ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
						ConnectionService.class, null);
				tracker.open();
				try
				{
					ConnectionService service = (ConnectionService) tracker.getService();
					if (service != null)
					{
						AddressGroupQuery query = (AddressGroupQuery) service.getQuery(AddressGroup.class);
						target = query.merge(target);
					}
				}
				finally
				{
					tracker.close();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean validateDrop(final Object target, final int operation, final TransferData transferType)
	{
		if (EntityTransfer.getTransfer().isSupportedType(transferType))
			return true;
		else if (CourseTransfer.getTransfer().isSupportedType(transferType))
			return true;
		else if (AddressGroupTransfer.getTransfer().isSupportedType(transferType))
			return true;
		return false;
	}
}
