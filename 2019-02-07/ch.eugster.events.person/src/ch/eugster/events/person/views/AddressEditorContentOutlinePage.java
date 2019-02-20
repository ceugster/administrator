package ch.eugster.events.person.views;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.BookingParticipantFilter;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressContact;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Contact;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.LinkPersonAddressContact;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.ContactQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.dialogs.BankAccountDialog;
import ch.eugster.events.person.dialogs.ContactDialog;
import ch.eugster.events.person.editors.AddressEditor;
import ch.eugster.events.person.editors.AddressEditorInput;
import ch.eugster.events.person.editors.PersonEditorInput;
import ch.eugster.events.person.editors.PersonFormEditor;
import ch.eugster.events.ui.views.IEntityEditorContentOutlinePage;

public class AddressEditorContentOutlinePage extends ContentOutlinePage implements IEntityEditorContentOutlinePage
{
	// private static final Calendar calendar = GregorianCalendar.getInstance();

	private static final DateFormat sdf = SimpleDateFormat.getDateInstance();

	private static final NumberFormat nf = DecimalFormat.getCurrencyInstance();

	private AddressEditor currentAddressEditor;

	private final EntityAdapter entityListener;

	public AddressEditorContentOutlinePage(final AddressEditor addressEditor)
	{
		this.currentAddressEditor = addressEditor;

		this.entityListener = new EntityAdapter() 
		{
			@Override
			public void postUpdate(AbstractEntity entity) 
			{
				if (entity instanceof BankAccount)
				{
					BankAccount bankAccount = (BankAccount) entity;
					List<BankAccount> accounts = bankAccount.getAddress().getBankAccounts();
					for (BankAccount account : accounts)
					{
						if (account.getId().equals(bankAccount.getId()))
						{
							account.getAddress().removeBankAccount(account);
							account.getAddress().addBankAccount(bankAccount);
						}
					}
					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
				if (entity instanceof AddressContact)
				{
					AddressContact updatedContact = (AddressContact) entity;
					List<AddressContact> contacts = updatedContact.getAddress().getContacts();
					for (AddressContact contact : contacts)
					{
						if (contact.getId().equals(updatedContact.getId()))
						{
							updatedContact.getAddress().removeContact(contact);
							updatedContact.getAddress().addContact(updatedContact);
						}
					}
					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
//				if (entity instanceof LinkPersonAddressContact)
//				{
//					LinkPersonAddressContact updatedContact = (LinkPersonAddressContact) entity;
//					List<LinkPersonAddressContact> contacts = updatedContact.getLink().getValidContacts();
//					for (LinkPersonAddressContact contact : contacts)
//					{
//						if (contact.getId().equals(updatedContact.getId()))
//						{
//							updatedContact.getLink().removeContact(contact);
//							updatedContact.getLink().addContact(updatedContact);
//						}
//					}
//					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
//				}
			}

			@Override
			public void postPersist(AbstractEntity entity) 
			{
				if (entity instanceof BankAccount)
				{
					BankAccount account = (BankAccount) entity;
					account.getAddress().addBankAccount(account);
					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
				else if (entity instanceof AddressContact)
				{
					AddressContact contact = (AddressContact) entity;
					contact.getAddress().addContact(contact);
					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
//				else if (entity instanceof LinkPersonAddressContact)
//				{
//					LinkPersonAddressContact contact = (LinkPersonAddressContact) entity;
//					contact.getLink().addContact(contact);
//					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
//				}
			}
			
			@Override
			public void postDelete(AbstractEntity entity)
			{
				if (entity instanceof AddressContact)
				{
					AddressContact contact = (AddressContact) entity;
					contact.getAddress().removeContact(contact);
					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
//				if (entity instanceof LinkPersonAddressContact)
//				{
//					LinkPersonAddressContact contact = (LinkPersonAddressContact) entity;
//					contact.getLink().removeContact(contact);
//					AddressEditorContentOutlinePage.this.getTreeViewer().refresh();
//				}
			}
		};
		EntityMediator.addListener(BankAccount.class, entityListener);
		EntityMediator.addListener(AddressContact.class, entityListener);
//		EntityMediator.addListener(LinkPersonAddressContact.class, entityListener);
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
				StructuredSelection ssel = (StructuredSelection) AddressEditorContentOutlinePage.this
						.getTreeViewer().getSelection();
				Object object = ssel.getFirstElement();
				if (object instanceof BankAccountRoot)
				{
					manager.add(AddressEditorContentOutlinePage.this.createAddBankAccountAction((BankAccountRoot) object));
				}
				else if (object instanceof BankAccount)
				{
					manager.add(AddressEditorContentOutlinePage.this.createEditBankAccountAction((BankAccount) object));
				}
				else if (object instanceof LinkPersonAddress)
				{
					manager.add(AddressEditorContentOutlinePage.this.createEditLinkAction((LinkPersonAddress) object));
				}
				if (object instanceof ContactRoot)
				{
					manager.add(AddressEditorContentOutlinePage.this.createAddContactAction((ContactRoot) object));
				}
				else if (object instanceof Contact)
				{
					manager.add(AddressEditorContentOutlinePage.this.createEditContactAction((Contact) object));
					manager.add(AddressEditorContentOutlinePage.this.createDeleteContactAction((Contact) object));
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
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.openEditor(new PersonEditorInput(person), PersonFormEditor.ID);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
					}
				}
			}

		});
		this.createContextMenu();

		this.setInput(this.currentAddressEditor);
	}

	private Action createEditLinkAction(final LinkPersonAddress link)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				AddressEditorContentOutlinePage.this.openEditor();
			}
		};

		action.setText("Bearbeiten");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EDIT"));
		return action;
	}

	private IAction createAddBankAccountAction(BankAccountRoot root)
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				Address address = ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity();
				BankAccount account = BankAccount.newInstance(address);
				Shell shell = AddressEditorContentOutlinePage.this.getSite().getShell();
				BankAccountDialog dialog = new BankAccountDialog(shell, account);
				dialog.open();
			}
		};
		action.setText("Hinzufügen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("ADD"));
		return action;
	}

	private IAction createAddContactAction(ContactRoot root)
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				Address address = ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity();
				Contact contact = AddressContact.newInstance(address);
				Shell shell = AddressEditorContentOutlinePage.this.getSite().getShell();
				ContactDialog dialog = new ContactDialog(shell, contact);
				dialog.open();
			}
		};
		action.setText("Hinzufügen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("ADD"));
		return action;
	}

	private IAction createEditBankAccountAction(final BankAccount account)
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				Shell shell = AddressEditorContentOutlinePage.this.getSite().getShell();
				BankAccountDialog dialog = new BankAccountDialog(shell, account);
				dialog.open();
			}
		};
		action.setText("Barbeiten");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.KEY_EDIT));
		return action;
	}

	private IAction createEditContactAction(final Contact contact)
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				Shell shell = AddressEditorContentOutlinePage.this.getSite().getShell();
				ContactDialog dialog = new ContactDialog(shell, contact);
				dialog.open();
			}
		};
		action.setText("Barbeiten");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.KEY_EDIT));
		return action;
	}

	private IAction createDeleteContactAction(final Contact contact)
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				Shell shell = AddressEditorContentOutlinePage.this.getSite().getShell();
				if (MessageDialog.openConfirm(shell, "Kontakt entfernen", "Wollen Sie den ausgewählten Kontakt entfernen?"))
				{
					ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
					tracker.open();
					try
					{
						ConnectionService service = tracker.getService();
						if (service != null)
						{
							ContactQuery query = (ContactQuery) service.getQuery(Contact.class);
							query.delete(contact);
						}
					}
					finally
					{
						tracker.close();
					}
				}
			}
		};
		action.setText("Entfernen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.KEY_DELETE));
		return action;
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(BankAccount.class, entityListener);
		EntityMediator.removeListener(AddressContact.class, entityListener);
		super.dispose();
	}

	private void openEditor()
	{
		StructuredSelection ssel = (StructuredSelection) AddressEditorContentOutlinePage.this.getTreeViewer()
				.getSelection();
		if (ssel.getFirstElement() instanceof Address)
		{
			Address address = (Address) ssel.getFirstElement();
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new AddressEditorInput(address), AddressEditor.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void setInput(final Object editor)
	{
		if (editor instanceof AddressEditor)
		{
			currentAddressEditor = (AddressEditor) editor;
		}
		getTreeViewer().setInput(this.currentAddressEditor);
	}

	private class AddressGroupMemberRoot implements Root
	{
		private final AddressEditor currentAddressEditor;

		private Map<Long, AddressGroupCategoryContainer> map;

		public AddressGroupMemberRoot(final AddressEditor editor)
		{
			this.currentAddressEditor = editor;
			this.map = new HashMap<Long, AddressGroupCategoryContainer>();
		}

		@Override
		public Object[] getChildren()
		{
			this.map.clear();
			List<AddressGroupMember> members = ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getAddressGroupMembers();
			for (AddressGroupMember member : members)
			{
				AddressGroupCategoryContainer container = map.get(member.getAddressGroup().getAddressGroupCategory().getId());
				if (container == null)
				{
					container = new AddressGroupCategoryContainer(member.getAddressGroup().getAddressGroupCategory());
					map.put(member.getAddressGroup().getAddressGroupCategory().getId(), container);
				}
				container.addMember(member);
			}
			return map.values().toArray(new AddressGroupCategoryContainer[0]);
		}

		@Override
		public AddressEditor getAddressEditor()
		{
			return currentAddressEditor;
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS_GROUP);
		}

		@Override
		public String getName()
		{
			return "Gruppenzugehörigkeiten";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.ADDRESS_GROUPS.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			return true;
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
			else if (element instanceof AddressGroupMember)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE);
			}
			else if (element instanceof Person || element instanceof LinkPersonAddress)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE);
			}
			else if (element instanceof BankAccount)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_BANK_CARD);
			}
			else if (element instanceof AddressGroupCategoryContainer)
			{
				return Activator.getDefault().getImageRegistry().get(Activator.KEY_CATEGORY);
			}
			else if (element instanceof Contact)
			{
				Contact contact = (Contact) element;
				return contact.getType().getProtocol().icon();
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
			else if (element instanceof AddressGroupCategoryContainer)
			{
				AddressGroupCategory category = ((AddressGroupCategoryContainer) element).getCategory();
				StringBuilder builder = new StringBuilder(category.getName());
				if (category.getDomain() != null && !category.getDomain().getName().isEmpty())
				{
					builder = builder.append(" (" + category.getDomain().getName() + ")");
				}
				return  builder.toString();
			}
			else if (element instanceof AddressGroupMember)
			{
				AddressGroup addressGroup = ((AddressGroupMember) element).getAddressGroup();
				return addressGroup.getCode() + " - " + addressGroup.getName();
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
				return account.getIban() + " (" + account.getAccountNumber() + ")";
			}
			else if (element instanceof Contact)
			{
				Contact contact = (Contact) element;
				String title = (contact.getName().isEmpty() ? (contact.getType().getName().isEmpty() ? contact.getType().getProtocol().label() : contact.getType().getName()) : contact.getName());
				return title + ": " + contact.getValue();
			}
			return "";
		}
	}

	private class AddressRoot implements Root
	{
		private final AddressEditor currentAddressEditor;

		public AddressRoot(final AddressEditor editor)
		{
			this.currentAddressEditor = editor;
		}

		@Override
		public Object[] getChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidLinks().toArray(new LinkPersonAddress[0]);
		}

		@Override
		public AddressEditor getAddressEditor()
		{
			return currentAddressEditor;
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS);
		}

		@Override
		public String getName()
		{
			return "Zugehörende Personen (" + getChildren().length + ")";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.PERSONS.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidLinks().size() > 0;
		}
	}

	private class DonationRoot implements Root
	{
		private final AddressEditor currentAddressEditor;

		public DonationRoot(final AddressEditor editor)
		{
			this.currentAddressEditor = editor;
		}

		@Override
		public Object[] getChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidDonations().toArray(new Donation[0]);
		}

		@Override
		public AddressEditor getAddressEditor()
		{
			return currentAddressEditor;
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_DONATION);
		}

		@Override
		public String getName()
		{
			return "Spenden (" + getChildren().length + ")";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.DONATION.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidDonations().size() > 0;
		}
	}

	private class ContactRoot implements Root
	{
		private final AddressEditor currentAddressEditor;

		public ContactRoot(final AddressEditor editor)
		{
			this.currentAddressEditor = editor;
		}

		@Override
		public Object[] getChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidContacts().toArray(new LinkPersonAddressContact[0]);
		}

		@Override
		public AddressEditor getAddressEditor()
		{
			return currentAddressEditor;
		}


		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_CONTACTS);
		}

		@Override
		public String getName()
		{
			return "Kontakte (" + getChildren().length + ")";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.CONTACTS.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidContacts().size() > 0;
		}
	}

	private class MemberRoot implements Root
	{
		private final AddressEditor currentAddressEditor;

		public MemberRoot(final AddressEditor editor)
		{
			this.currentAddressEditor = editor;
		}

		@Override
		public Object[] getChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidMembers().toArray(new Member[0]);
		}

		@Override
		public AddressEditor getAddressEditor()
		{
			return currentAddressEditor;
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_PARTICIPANT);
		}

		@Override
		public String getName()
		{
			return "Mitgliedschaften: " + getChildren().length + ")";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.MEMBER.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getValidMembers().size() > 0;
		}
	}

	public enum Order
	{
		PERSONS, CONTACTS, BANK_ACCOUNT, COURSES, DONATION, MEMBER, ADDRESS_GROUPS;
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
			if (parentElement instanceof AddressEditor)
			{
				/*
				 * Es wurde ein AdressEditor ausgewählt
				 */
				AddressEditor editor = (AddressEditor) parentElement;
				List<Root> roots = new ArrayList<Root>();
				roots.add(new AddressRoot(editor));
				roots.add(new MemberRoot(editor));
				roots.add(new DonationRoot(editor));
				roots.add(new AddressGroupMemberRoot(editor));
				roots.add(new BankAccountRoot(editor));
				roots.add(new ContactRoot(editor));
				return roots.toArray(new Root[0]);
			}
			else if (parentElement instanceof Root)
			{
				Root root = (Root) parentElement;
				return root.getChildren();
			}
			else if (parentElement instanceof AddressGroupCategoryContainer)
			{
				AddressGroupCategoryContainer container = (AddressGroupCategoryContainer) parentElement;
				return container.getMembers();
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
			if (element instanceof AddressGroupCategoryContainer)
			{
				return ((AddressGroupCategoryContainer) element).getMembers().length != 0;
			}
			return false;
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}

	}

	private class BankAccountRoot implements Root
	{
		private final AddressEditor currentAddressEditor;

		public BankAccountRoot(final AddressEditor editor)
		{
			this.currentAddressEditor = editor;
		}

		@Override
		public Object[] getChildren()
		{
			return getBankAccounts().toArray(new BankAccount[0]);
		}

		@Override
		public AddressEditor getAddressEditor()
		{
			return currentAddressEditor;
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_BANK_CARD);
		}

		@Override
		public String getName()
		{
			return "Bankverbindungen (" + getBankAccounts().size() + ")";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.BANK_ACCOUNT.ordinal());
		}

		private List<BankAccount> getBankAccounts()
		{
			return ((AddressEditorInput) currentAddressEditor.getEditorInput()).getEntity().getBankAccounts();
		}

		@Override
		public boolean hasChildren()
		{
			return getBankAccounts().size() > 0;
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

	interface Root
	{
		Object[] getChildren();

		AddressEditor getAddressEditor();

		Image getImage();

		String getName();

		Integer getOrder();

		boolean hasChildren();
	}
	
	class AddressGroupCategoryContainer
	{
		private AddressGroupCategory category;
		
		private List<AddressGroupMember> members;
		
		public AddressGroupCategoryContainer(AddressGroupCategory category)
		{
			this.category = category;
			this.members = new ArrayList<AddressGroupMember>();
		}
		
		public AddressGroupCategory getCategory()
		{
			return this.category;
		}
		
		public void addMember(AddressGroupMember member)
		{
			members.add(member);
		}
		
		public AddressGroupMember[] getMembers()
		{
			return this.members.toArray(new AddressGroupMember[0]);
		}
	}

	@Override
	public void update() 
	{
		// TODO Auto-generated method stub
		
	}
}
