package ch.eugster.events.person.views;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import ch.eugster.events.persistence.filters.BookingParticipantFilter;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.LinkPersonAddressEditorInput;
import ch.eugster.events.person.editors.PersonEditor;
import ch.eugster.events.person.editors.PersonFormEditor;
import ch.eugster.events.ui.views.IEntityEditorContentOutlinePage;

public class PersonEditorOutlinePage extends ContentOutlinePage implements IEntityEditorContentOutlinePage
{
	private static final DateFormat sdf = SimpleDateFormat.getDateInstance();

	private static final NumberFormat nf = DecimalFormat.getCurrencyInstance();

	private PersonEditor personEditor;

	public PersonEditorOutlinePage(final PersonEditor personEditor)
	{
		this.personEditor = personEditor;
	}

	protected void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener()
		{
			@Override
			public void menuAboutToShow(final IMenuManager manager)
			{
				StructuredSelection ssel = (StructuredSelection) PersonEditorOutlinePage.this.getTreeViewer()
						.getSelection();
				Object object = ssel.getFirstElement();
				if (object instanceof LinkPersonAddress)
				{
					PersonEditorOutlinePage.this.createEditLinkAction(manager, (LinkPersonAddress) object);
				}
			}
		});

		Menu menu = menuManager.createContextMenu(this.getTreeViewer().getControl());
		this.getTreeViewer().getControl().setMenu(menu);

		this.getSite().registerContextMenu(ContentOutline.PREFIX, menuManager, this.getTreeViewer());
	}

	@Override
	public void createControl(final Composite parent)
	{
		super.createControl(parent);

		TreeViewer viewer = this.getTreeViewer();
		viewer.setContentProvider(new OutlineContentProvider());
		viewer.setLabelProvider(new AddressLabelProvider());
		viewer.setSorter(new PersonViewerSorter());
		viewer.setFilters(new ViewerFilter[] { new BookingParticipantFilter(), new DeletedEntityFilter() });
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(final DoubleClickEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
					PersonFormEditor editor = (PersonFormEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().getActiveEditor();
					editor.setActivePage("link.page." + link.getAddressType().getId().toString());
				}
				else if (ssel.getFirstElement() instanceof Person)
				{
					Person person = (Person) ssel.getFirstElement();
					try
					{
						PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.openEditor(new LinkPersonAddressEditorInput(person.getDefaultLink()),
										PersonFormEditor.ID);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
					}
				}
			}

		});
		this.createContextMenu();

		this.setInput(personEditor);
	}

	private void createEditLinkAction(final IMenuManager manager, final LinkPersonAddress link)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				PersonEditorOutlinePage.this.openEditor();
			}
		};

		action.setText("Bearbeiten");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EDIT"));
		manager.add(action);
	}

	private void openEditor()
	{
		StructuredSelection ssel = (StructuredSelection) PersonEditorOutlinePage.this.getTreeViewer().getSelection();
		if (ssel.getFirstElement() instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			if (input.getEntity().getId() == null || !input.getEntity().getId().equals(link.getId()))
			{
				try
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.openEditor(new LinkPersonAddressEditorInput(link), PersonFormEditor.ID);
				}
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void setInput(final Object personEditor)
	{
		getTreeViewer().setInput(personEditor);
	}

	@Override
	public void update()
	{
	}

	private class AddressGroupMemberRoot implements Root
	{
		private final PersonEditor personEditor;

		public AddressGroupMemberRoot(final PersonEditor personEditor)
		{
			this.personEditor = personEditor;
		}

		@Override
		public Object[] getChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getAddressGroupMembers().toArray(new AddressGroupMember[0]);
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS_GROUP_MEMBER);
		}

		@Override
		public String getName()
		{
			return "Adressgruppen";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.ADDRESS_GROUPS.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getAddressGroupMembers().size() > 0;
		}
	}

	private class AddressLabelProvider extends LabelProvider
	{
		@Override
		public Image getImage(final Object element)
		{
			if (element instanceof Root)
			{
				return ((Root) element).getImage();
			}
			else if (element instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) element;
				Image image = link.getAddressType() == null ? null : link.getAddressType().getImage();
				return image;
			}
			else if (element instanceof Donation)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_DONATION);
			}
			else if (element instanceof Participant)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_PARTICIPANT);
			}
			else if (element instanceof Person || element instanceof LinkPersonAddress)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE);
			}
			else if (element instanceof AddressGroupMember)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE);
			}
			return null;
		}

		@Override
		public String getText(final Object element)
		{
			if (element instanceof Root)
			{
				return ((Root) element).getName();
			}
			else if (element instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) element;
				return PersonFormatter.getInstance().formatLastnameFirstname(link.getPerson());
			}
			else if (element instanceof Person)
			{
				Person person = (Person) element;
				return PersonFormatter.getInstance().formatId(person) + " "
						+ PersonFormatter.getInstance().formatLastnameFirstname(person);
			}
			else if (element instanceof Donation)
			{
				Donation donation = (Donation) element;
				return sdf.format(donation.getDonationDate().getTime()) + " " + nf.format(donation.getAmount());
			}
			else if (element instanceof Member)
			{
				Member member = (Member) element;
				return member.getMembership().format();
			}
			else if (element instanceof AddressGroupMember)
			{
				AddressGroupMember member = (AddressGroupMember) element;
				String code = member.getAddressGroup().getCode();
				String name = member.getAddressGroup().getName();
				return code.isEmpty() ? name : code + " - " + name;
			}
			else if (element instanceof Participant)
			{
				String date = null;
				Participant participant = (Participant) element;
				Booking booking = participant.getBooking();
				Course course = booking.getCourse();
				CourseDetail[] courseDetails = course.getCourseDetails().toArray(new CourseDetail[0]);
				if (courseDetails.length > 0 && courseDetails[0].getStart() != null)
				{
					date = sdf.format(courseDetails[0].getStart().getTime());
				}
				return course.getCode() + " " + course.getTitle() + (date == null ? "" : date);
			}
			else if (element instanceof BankAccount)
			{
				BankAccount account = (BankAccount) element;
				return account.getIban();
			}
			return "";
		}
	}

	private class DonationRoot implements Root
	{
		private final PersonEditor personEditor;

		public DonationRoot(final PersonEditor personEditor)
		{
			this.personEditor = personEditor;
		}

		@Override
		public Object[] getChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getPerson().getDonations().toArray(new Donation[0]);
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_DONATION);
		}

		@Override
		public String getName()
		{
			return "Spenden";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.DONATION.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getPerson().getDonations().size() > 0;
		}
	}

	private class MemberRoot implements Root
	{
		private final PersonEditor personEditor;

		public MemberRoot(final PersonEditor personEditor)
		{
			this.personEditor = personEditor;
		}

		@Override
		public Object[] getChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getPerson().getMembers().toArray(new Member[0]);
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_PARTICIPANT);
		}

		@Override
		public String getName()
		{
			return "Mitgliedschaften";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.MEMBER.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getPerson().getMembers().toArray(new Member[0]).length > 0;
		}
	}

	public enum Order
	{
		PERSONS, ADDRESS_GROUPS, COURSES, DONATION, MEMBER, BANK_ACCOUNTS;
	}

	private class OutlineContentProvider implements ITreeContentProvider
	{
		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getChildren(final Object parentElement)
		{
			if (parentElement instanceof PersonEditor)
			{
				PersonEditor editor = (PersonEditor) parentElement;
				LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) editor.getEditorInput();
				LinkPersonAddress link = input.getEntity();
				List<Root> roots = new ArrayList<Root>();
				if (link.getPerson().getMembers().size() > 0)
				{
					roots.add(new MemberRoot(editor));
				}
				if (link.getParticipants().size() > 0)
				{
					roots.add(new ParticipantRoot(editor));
				}
				if (link.getPerson().getDonations().size() > 0)
				{
					roots.add(new DonationRoot(editor));
				}
				if (link.getAddressGroupMembers().size() > 0)
				{
					roots.add(new AddressGroupMemberRoot(editor));
				}
				if (link.getPerson().getValidBankAccounts().size() > 0)
				{
					roots.add(new BankAccountRoot(editor));
				}
				return roots.toArray(new Root[0]);
			}
			else if (parentElement instanceof Root)
			{
				Root root = (Root) parentElement;
				return root.getChildren();
			}
			return new Object[0];
		}

		@Override
		public Object[] getElements(final Object inputElement)
		{
			return this.getChildren(inputElement);
		}

		@Override
		public Object getParent(final Object element)
		{
			if (element instanceof LinkPersonAddress)
			{
				return ((LinkPersonAddress) element).getPerson();
			}
			return null;
		}

		@Override
		public boolean hasChildren(final Object element)
		{
			if (element instanceof Root)
			{
				return ((Root) element).hasChildren();
			}
			else if (element instanceof LinkPersonAddress)
			{
				LinkPersonAddress lnk = (LinkPersonAddress) element;
				List<Person> persons = new ArrayList<Person>();
				List<LinkPersonAddress> links = lnk.getAddress().getPersonLinks();
				for (LinkPersonAddress link : links)
				{
					if (!link.isDeleted())
					{
						if (!link.getId().equals(lnk.getId()))
						{
							persons.add(link.getPerson());
						}
					}
				}
				return persons.size() > 0;
			}
			return false;
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}

	}

	private class ParticipantRoot implements Root
	{
		private final PersonEditor personEditor;

		public ParticipantRoot(final PersonEditor personEditor)
		{
			this.personEditor = personEditor;
		}

		@Override
		public Object[] getChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getParticipants().toArray(new Participant[0]);
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_PARTICIPANT);
		}

		@Override
		public String getName()
		{
			return "Besuchte Kurse";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.COURSES.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getParticipants().toArray(new Participant[0]).length > 0;
		}
	}

	private class BankAccountRoot implements Root
	{
		private final PersonEditor personEditor;

		public BankAccountRoot(final PersonEditor personEditor)
		{
			this.personEditor = personEditor;
		}

		@Override
		public Object[] getChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getPerson().getValidBankAccounts().toArray(new BankAccount[0]);
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_BANK_CARD);
		}

		@Override
		public String getName()
		{
			return "Bankverbindungen";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.BANK_ACCOUNTS.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) personEditor.getEditorInput();
			LinkPersonAddress link = input.getEntity();
			return link.getPerson().getValidBankAccounts().toArray(new BankAccount[0]).length > 0;
		}
	}

	private class PersonViewerSorter extends ViewerSorter
	{
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2)
		{
			if (e1 instanceof Root && e2 instanceof Root)
			{
				Root r1 = (Root) e1;
				Root r2 = (Root) e2;
				return r1.getOrder().compareTo(r2.getOrder());
			}
			else if (e1 instanceof Donation && e2 instanceof Donation)
			{
				Donation d1 = (Donation) e1;
				Donation d2 = (Donation) e2;
				return d2.getDonationDate().compareTo(d1.getDonationDate());
			}
			else if (e1 instanceof Participant && e2 instanceof Participant)
			{
				Participant p1 = (Participant) e1;
				Participant p2 = (Participant) e2;
				return p2.getBooking().getCourse().getCode().compareTo(p1.getBooking().getCourse().getCode());
			}
			return 0;
		}

	}

	private interface Root
	{
		Object[] getChildren();

		Image getImage();

		String getName();

		Integer getOrder();

		boolean hasChildren();
	}

}
