package ch.eugster.events.addressgroup.handlers;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.dnd.AddressGroupMemberTransfer;
import ch.eugster.events.addressgroup.dnd.AddressGroupTransfer;
import ch.eugster.events.addressgroup.views.AddressGroupMemberView;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.helpers.ClipboardHelper;

public class PasteHandler extends AbstractHandler implements IHandler
{
	private void execute(final AddressGroupMemberView view)
	{
		TableViewer viewer = view.getViewer();
		AddressGroup target = (AddressGroup) viewer.getInput();
		String[] names = ClipboardHelper.getClipboard().getAvailableTypeNames();
		for (String name : names)
		{
			if (name.equals(AddressGroupTransfer.TYPE_NAME))
			{
				Object object = ClipboardHelper.getClipboard().getContents(AddressGroupTransfer.getTransfer());
				if (object instanceof AddressGroup[])
				{
					AddressGroup[] addressGroups = (AddressGroup[]) object;
					updateCategories(insertAddressGroups(target, addressGroups, AddressGroupTransfer.getTransfer()
							.getOperation()));
				}
			}
			else if (name.equals(AddressGroupMemberTransfer.TYPE_NAME))
			{
				Object object = ClipboardHelper.getClipboard().getContents(AddressGroupMemberTransfer.getTransfer());
				if (object instanceof AddressGroupMember[])
				{
					AddressGroupMember[] members = (AddressGroupMember[]) object;
					this.insertAddressGroupMembers(target, members, AddressGroupMemberTransfer.getTransfer()
							.getOperation());
				}
			}
			// else if (name.equals(CourseTransfer.TYPE_NAME))
			// {
			// Object content =
			// ClipboardHelper.getClipboard().getContents(CourseTransfer.getTransfer());
			// if (content instanceof Object[])
			// {
			// Collection<Booking> bookings = getBookings((Object[]) content);
			// if (!bookings.isEmpty())
			// {
			// this.insert(target, bookings.toArray(new Booking[0]),
			// CourseTransfer.getTransfer()
			// .getOperation());
			// }
			// }
			// }
		}
	}

	private void execute(final AddressGroupView view)
	{
		TreeViewer viewer = view.getViewer();
		if (!viewer.getSelection().isEmpty())
		{
			if (viewer.getSelection() instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
				if (ssel.size() == 1)
				{
					if (ssel.getFirstElement() instanceof AddressGroupCategory)
					{
						AddressGroupCategory target = (AddressGroupCategory) ssel.getFirstElement();
						String[] names = ClipboardHelper.getClipboard().getAvailableTypeNames();
						for (String name : names)
						{
							if (name.equals(AddressGroupTransfer.TYPE_NAME))
							{
								Object object = ClipboardHelper.getClipboard().getContents(
										AddressGroupTransfer.getTransfer());
								if (object instanceof AddressGroup[])
								{
									AddressGroup[] addressGroups = (AddressGroup[]) object;
									this.updateCategories(insertAddressGroups(target, addressGroups,
											AddressGroupTransfer.getTransfer().getOperation()));
								}
							}
						}
					}
					else if (ssel.getFirstElement() instanceof AddressGroup)
					{
						AddressGroup target = (AddressGroup) ssel.getFirstElement();
						String[] names = ClipboardHelper.getClipboard().getAvailableTypeNames();
						for (String name : names)
						{
							if (name.equals(AddressGroupTransfer.TYPE_NAME))
							{
								Object object = ClipboardHelper.getClipboard().getContents(
										AddressGroupTransfer.getTransfer());
								if (object instanceof AddressGroup[])
								{
									AddressGroup[] addressGroups = (AddressGroup[]) object;
									this.updateCategories(insertAddressGroups(target, addressGroups,
											AddressGroupTransfer.getTransfer().getOperation()));
								}
							}
							else if (name.equals(AddressGroupMemberTransfer.TYPE_NAME))
							{
								Object object = ClipboardHelper.getClipboard().getContents(
										AddressGroupMemberTransfer.getTransfer());
								if (object instanceof AddressGroupMember[])
								{
									AddressGroupMember[] members = (AddressGroupMember[]) object;
									this.updateCategories(insertAddressGroupMembers(target, members,
											AddressGroupMemberTransfer.getTransfer().getOperation()));
								}
							}
							else if (name.equals(CourseTransfer.TYPE_NAME))
							{
								Object contents = ClipboardHelper.getClipboard().getContents(
										CourseTransfer.getTransfer());
								if (contents instanceof Object[])
								{
									boolean updateCategory = false;
									Object[] objects = (Object[]) contents;
									for (Object object : objects)
									{
										if (object instanceof Season)
										{
											Season season = (Season) object;
											if (insertSeason(target, season, CourseTransfer.getTransfer()
													.getOperation()))
											{
												updateCategory = true;
											}
										}
										else if (object instanceof Course)
										{
											Course course = (Course) object;
											if (insertCourse(target, course, CourseTransfer.getTransfer()
													.getOperation()))
											{
												updateCategory = true;
											}
										}
										else if (object instanceof Booking)
										{
											Booking booking = (Booking) object;
											if (insertBooking(target, booking, CourseTransfer.getTransfer()
													.getOperation()))
											{
												updateCategory = true;
											}
										}
										else if (object instanceof Participant)
										{
											Participant participant = (Participant) object;
											if (insertParticipant(target, participant, CourseTransfer.getTransfer()
													.getOperation()))
											{
												updateCategory = true;
											}
										}
									}
									if (updateCategory)
									{
										this.updateCategories(new AddressGroupCategory[] { target
												.getAddressGroupCategory() });
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			IWorkbenchPart part = (IWorkbenchPart) context.getVariable("activePart");
			if (part instanceof AddressGroupMemberView)
			{
				execute((AddressGroupMemberView) part);
			}
			else if (part instanceof AddressGroupView)
			{
				execute((AddressGroupView) part);
			}
		}
		return Status.OK_STATUS;
	}

	// private Collection<Booking> extractBookings(final Course course)
	// {
	// Collection<Booking> bookings = new ArrayList<Booking>();
	// if (!course.isDeleted())
	// {
	// for (Booking booking : course.getBookings())
	// {
	// if (!booking.isDeleted())
	// {
	// bookings.add(booking);
	// }
	// }
	// }
	// return bookings;
	// }

	// private Collection<Booking> extractBookings(final Season season)
	// {
	// Collection<Booking> bookings = new ArrayList<Booking>();
	// if (!season.isDeleted())
	// {
	// Collection<Course> courses = season.getCourses();
	// for (Course course : courses)
	// {
	// bookings.addAll(this.extractBookings(course));
	// }
	// }
	// return bookings;
	// }

	private boolean found(final AddressGroup target, final LinkPersonAddress link)
	{
		boolean found = false;
		Collection<AddressGroupMember> targetMembers = target.getAddressGroupMembers();
		for (AddressGroupMember targetMember : targetMembers)
		{
			if (targetMember.getLink() != null)
			{
				if (targetMember.getLink().getPerson().getId().equals(link.getPerson().getId()))
				{
					found = true;
					break;
				}
			}
		}
		return found;
	}

	private boolean found(final AddressGroup target, final AddressGroupMember member)
	{
		if (member.getLink() == null)
		{
			return found(target, member.getAddress());
		}
		else
		{
			return found(target, member.getLink());
		}
	}

	private boolean found(final AddressGroup target, final Address address)
	{
		boolean found = false;
		Collection<AddressGroupMember> targetMembers = target.getAddressGroupMembers();
		for (AddressGroupMember targetMember : targetMembers)
		{
			if (targetMember.getLink() != null)
			{
				if (targetMember.getAddress().getId().equals(address.getId()))
				{
					found = true;
					break;
				}
			}
		}
		return found;
	}

	private boolean found(final AddressGroupCategory target, final AddressGroup addressGroup)
	{
		boolean found = false;
		Collection<AddressGroup> targetAddressGroups = target.getAddressGroups();
		for (AddressGroup targetAddressGroup : targetAddressGroups)
		{
			if (targetAddressGroup.getId().equals(addressGroup.getId()))
			{
				found = true;
				break;
			}
		}
		return found;
	}

	// private AddressGroupMember[] getAddressGroupMembers(final AddressGroup[]
	// addressGroups)
	// {
	// Map<Long, AddressGroupMember> addressMembers = new HashMap<Long,
	// AddressGroupMember>();
	// Map<Long, AddressGroupMember> linkMembers = new HashMap<Long,
	// AddressGroupMember>();
	// for (AddressGroup addressGroup : addressGroups)
	// {
	// Collection<AddressGroupMember> ms = addressGroup.getMembers();
	// for (AddressGroupMember m : ms)
	// {
	// if (m.getLink() == null)
	// {
	// if (addressMembers.get(m.getAddress().getId()) == null)
	// {
	// addressMembers.put(m.getAddress().getId(), m);
	// }
	// }
	// else
	// {
	// if (linkMembers.get(m.getLink().getId()) == null)
	// {
	// linkMembers.put(m.getLink().getId(), m);
	// }
	// }
	// }
	// }
	// Collection<AddressGroupMember> members = new
	// ArrayList<AddressGroupMember>();
	// members.addAll(addressMembers.values());
	// members.addAll(linkMembers.values());
	// return members.toArray(new AddressGroupMember[0]);
	// }

	// private Collection<Booking> getBookings(final Object[] content)
	// {
	// Collection<Booking> bookings = new ArrayList<Booking>();
	// Object[] elements = content;
	// for (Object element : elements)
	// {
	// if (element instanceof Season)
	// {
	// Season season = (Season) element;
	// bookings.addAll(this.extractBookings(season));
	// }
	// else if (element instanceof Course)
	// {
	// Course course = (Course) element;
	// bookings.addAll(this.extractBookings(course));
	// }
	// if (element instanceof Booking)
	// {
	// Booking booking = (Booking) element;
	// bookings.add(booking);
	// }
	// }
	// return bookings;
	// }

	private boolean insertAddressGroup(final AddressGroup target, final AddressGroup source, final int type)
	{
		boolean inserted = false;
		if (!source.getId().equals(target.getId()))
		{
			for (AddressGroupMember sourceMember : source.getAddressGroupMembers())
			{
				if (!sourceMember.isDeleted())
				{
					if (insertAddressGroupMember(target, sourceMember, type))
					{
						inserted = true;
					}
				}
			}
		}
		return inserted;
	}

	private boolean insertAddressGroup(final AddressGroupCategory target, final AddressGroup addressGroup,
			final int type)
	{
		boolean inserted = false;
		if (!addressGroup.isDeleted())
		{
			if (!found(target, addressGroup))
			{
				inserted = true;
				target.addAddressGroup(addressGroup.copy(target));
			}
			if (type == DND.DROP_MOVE)
			{
				addressGroup.setDeleted(true);
			}
		}
		return inserted;
	}

	private boolean insertAddressGroupMember(final AddressGroup target, final AddressGroupMember sourceMember,
			final int type)
	{
		boolean inserted = false;
		if (!sourceMember.isDeleted())
		{
			if (!found(target, sourceMember))
			{
				inserted = true;
				target.addAddressGroupMember(sourceMember.copy(target));
			}
			if (type == DND.DROP_MOVE)
			{
				sourceMember.setDeleted(true);
			}
		}
		return inserted;
	}

	// private void insert(final AddressGroup target, final Booking[] bookings,
	// final int type)
	// {
	// Collection<AddressGroupMember> members = new
	// ArrayList<AddressGroupMember>();
	// for (Booking booking : bookings)
	// {
	// Participant participant = booking.getParticipant();
	// if (!participant.isDeleted() && !participant.getLink().isDeleted())
	// {
	// members.add(AddressGroupMember.newInstance(target,
	// participant.getLink()));
	// }
	// }
	// this.insert(target, members.toArray(new AddressGroupMember[0]), type);
	// }

	private AddressGroupCategory[] insertAddressGroupMembers(final AddressGroup target,
			final AddressGroupMember[] members, final int type)
	{
		Collection<AddressGroupCategory> categoriesToUpdate = new ArrayList<AddressGroupCategory>();
		for (AddressGroupMember member : members)
		{
			if (!member.isDeleted())
			{
				if (insertAddressGroupMember(target, member, type))
				{
					if (!categoriesToUpdate.contains(target.getAddressGroupCategory()))
					{
						categoriesToUpdate.add(target.getAddressGroupCategory());
					}
					if (type == DND.DROP_MOVE)
					{
						if (!categoriesToUpdate.contains(member.getAddressGroup().getAddressGroupCategory()))
						{
							categoriesToUpdate.add(member.getAddressGroup().getAddressGroupCategory());
						}
					}
				}
			}
		}
		return categoriesToUpdate.toArray(new AddressGroupCategory[0]);
	}

	private boolean insertSeason(final AddressGroup target, final Season season, final int type)
	{
		boolean inserted = false;
		if (!season.isDeleted())
		{
			for (Course course : season.getCourses())
			{
				if (insertCourse(target, course, type))
				{
					inserted = true;
				}
			}
		}
		return inserted;
	}

	private boolean insertCourse(final AddressGroup target, final Course course, final int type)
	{
		boolean inserted = false;
		if (!course.isDeleted())
		{
			for (Booking booking : course.getBookings())
			{
				if (insertBooking(target, booking, type))
				{
					inserted = true;
				}
			}
		}
		return inserted;
	}

	private boolean insertBooking(final AddressGroup target, final Booking booking, final int type)
	{
		boolean inserted = false;
		if (!booking.isDeleted())
		{
			for (Participant participant : booking.getParticipants())
			{
				if (insertParticipant(target, participant, type))
				{
					inserted = true;
				}
			}
		}
		return inserted;
	}

	private boolean insertParticipant(final AddressGroup target, final Participant participant, final int type)
	{
		boolean inserted = false;
		if (!participant.isDeleted())
		{
			if (!found(target, participant.getLink()))
			{
				inserted = true;
				AddressGroupMember member = AddressGroupMember.newInstance(target, participant.getLink());
				target.addAddressGroupMember(member);
			}
		}
		return inserted;
	}

	// private boolean insertAddressGroupMembers(final AddressGroupCategory
	// target, final AddressGroup source,
	// final int type)
	// {
	// boolean inserted = false;
	// if (!source.getId().equals(target.getId()))
	// {
	// for (AddressGroupMember sourceMember : source.getMembers())
	// {
	// if (!sourceMember.isDeleted())
	// {
	// if (insert(target, sourceMember, type))
	// {
	// inserted = true;
	// }
	// }
	// }
	// }
	// return inserted;
	// }

	private AddressGroupCategory[] insertAddressGroups(final AddressGroup target, final AddressGroup[] sources,
			final int type)
	{
		Collection<AddressGroupCategory> categoriesToUpdate = new ArrayList<AddressGroupCategory>();
		for (AddressGroup source : sources)
		{
			if (!source.isDeleted())
			{
				if (insertAddressGroup(target, source, type))
				{
					if (!categoriesToUpdate.contains(target.getAddressGroupCategory()))
					{
						categoriesToUpdate.add(target.getAddressGroupCategory());
					}
					if (type == DND.DROP_MOVE)
					{
						if (!categoriesToUpdate.contains(source.getAddressGroupCategory()))
						{
							categoriesToUpdate.add(source.getAddressGroupCategory());
						}
					}
				}
			}
		}
		return categoriesToUpdate.toArray(new AddressGroupCategory[0]);
	}

	private AddressGroupCategory[] insertAddressGroups(final AddressGroupCategory target, final AddressGroup[] sources,
			final int type)
	{
		Collection<AddressGroupCategory> categoriesToUpdate = new ArrayList<AddressGroupCategory>();
		for (AddressGroup source : sources)
		{
			if (!source.isDeleted())
			{
				if (!target.getId().equals(source.getAddressGroupCategory().getId()))
				{
					if (insertAddressGroup(target, source, type))
					{
						if (!categoriesToUpdate.contains(target))
						{
							categoriesToUpdate.add(target);
						}
						if (type == DND.DROP_MOVE)
						{
							if (!categoriesToUpdate.contains(source.getAddressGroupCategory()))
							{
								categoriesToUpdate.add(source.getAddressGroupCategory());
							}
						}
					}
				}
			}
		}
		return categoriesToUpdate.toArray(new AddressGroupCategory[0]);
	}

	private boolean isAddressGroupMemberTransfer()
	{
		String[] names = ClipboardHelper.getClipboard().getAvailableTypeNames();
		for (String name : names)
		{
			if (name.equals(AddressGroupMemberTransfer.TYPE_NAME))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isAddressGroupTransfer()
	{
		String[] names = ClipboardHelper.getClipboard().getAvailableTypeNames();
		for (String name : names)
		{
			if (name.equals(AddressGroupTransfer.TYPE_NAME))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isCourseTransfer()
	{
		String[] names = ClipboardHelper.getClipboard().getAvailableTypeNames();
		for (String name : names)
		{
			if (name.equals(CourseTransfer.TYPE_NAME))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isValidTransfer()
	{
		return isAddressGroupMemberTransfer() || isAddressGroupTransfer() || isCourseTransfer();
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		if (evaluationContext instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) evaluationContext;
			Object object = context.getVariable("activePart");
			if (object instanceof AddressGroupMemberView)
			{
				AddressGroupMemberView view = (AddressGroupMemberView) object;
				TableViewer viewer = view.getViewer();
				if (viewer.getInput() instanceof AddressGroup)
				{
					enabled = isValidTransfer();
				}
			}
			else if (object instanceof AddressGroupView)
			{
				AddressGroupView view = (AddressGroupView) object;
				TreeViewer viewer = view.getViewer();
				if (!viewer.getSelection().isEmpty())
				{
					if (viewer.getSelection() instanceof StructuredSelection)
					{
						StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
						if (ssel.size() == 1)
						{
							if (ssel.getFirstElement() instanceof AddressGroupCategory)
							{
								enabled = isAddressGroupTransfer();
							}
							else if (ssel.getFirstElement() instanceof AddressGroup)
							{
								enabled = isValidTransfer();
							}
						}
					}
				}
			}
		}
		setBaseEnabled(enabled);
	}

	private void updateCategories(final AddressGroupCategory[] categories)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) service
						.getQuery(AddressGroupCategory.class);
				for (AddressGroupCategory category : categories)
				{
					category = query.merge(category);
				}
			}
		}
		finally
		{
			tracker.close();
		}
	}

}
