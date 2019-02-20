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
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
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
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.BookingParticipantFilter;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
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
import ch.eugster.events.persistence.model.PersonContact;
import ch.eugster.events.persistence.queries.ContactQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.dialogs.BankAccountDialog;
import ch.eugster.events.person.dialogs.ContactDialog;
import ch.eugster.events.person.editors.FormEditorLinkPage;
import ch.eugster.events.person.editors.FormEditorPersonPage;
import ch.eugster.events.person.editors.PersonEditorInput;
import ch.eugster.events.person.editors.PersonFormEditor;

public class PersonFormEditorContentOutlinePage extends ContentOutlinePage implements IPageChangedListener
{
	// private static final Calendar calendar = GregorianCalendar.getInstance();

	private static final DateFormat sdf = SimpleDateFormat.getDateInstance();

	private static final NumberFormat nf = DecimalFormat.getCurrencyInstance();

	private FormPage currentPage;

	private final EntityAdapter entityListener;

	public PersonFormEditorContentOutlinePage(final FormPage page)
	{
		this.currentPage = page;
		this.currentPage.getEditor().addPageChangedListener(this);

		this.entityListener = new EntityAdapter() 
		{
			@Override
			public void postUpdate(AbstractEntity entity) 
			{
				if (entity instanceof BankAccount)
				{
					BankAccount bankAccount = (BankAccount) entity;
					List<BankAccount> accounts = bankAccount.getPerson().getValidBankAccounts();
					for (BankAccount account : accounts)
					{
						if (account.getId().equals(bankAccount.getId()))
						{
							account.getPerson().removeBankAccount(account);
							account.getPerson().addBankAccount(bankAccount);
						}
					}
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
				if (entity instanceof PersonContact)
				{
					PersonContact updatedContact = (PersonContact) entity;
					List<PersonContact> contacts = updatedContact.getPerson().getValidContacts();
					for (PersonContact contact : contacts)
					{
						if (contact.getId().equals(updatedContact.getId()))
						{
							updatedContact.getPerson().removeContact(contact);
							updatedContact.getPerson().addContact(updatedContact);
						}
					}
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
				if (entity instanceof LinkPersonAddressContact)
				{
					LinkPersonAddressContact updatedContact = (LinkPersonAddressContact) entity;
					List<LinkPersonAddressContact> contacts = updatedContact.getLink().getValidContacts();
					for (LinkPersonAddressContact contact : contacts)
					{
						if (contact.getId().equals(updatedContact.getId()))
						{
							updatedContact.getLink().removeContact(contact);
							updatedContact.getLink().addContact(updatedContact);
						}
					}
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
			}

			@Override
			public void postPersist(AbstractEntity entity) 
			{
				if (entity instanceof BankAccount)
				{
					BankAccount account = (BankAccount) entity;
					account.getPerson().addBankAccount(account);
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
				else if (entity instanceof PersonContact)
				{
					PersonContact contact = (PersonContact) entity;
					contact.getPerson().addContact(contact);
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
				else if (entity instanceof LinkPersonAddressContact)
				{
					LinkPersonAddressContact contact = (LinkPersonAddressContact) entity;
					contact.getLink().addContact(contact);
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
			}
			
			@Override
			public void postDelete(AbstractEntity entity)
			{
				if (entity instanceof PersonContact)
				{
					PersonContact contact = (PersonContact) entity;
					contact.getPerson().removeContact(contact);
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
				if (entity instanceof LinkPersonAddressContact)
				{
					LinkPersonAddressContact contact = (LinkPersonAddressContact) entity;
					contact.getLink().removeContact(contact);
					PersonFormEditorContentOutlinePage.this.getTreeViewer().refresh();
				}
			}
		};
		EntityMediator.addListener(BankAccount.class, entityListener);
		EntityMediator.addListener(PersonContact.class, entityListener);
		EntityMediator.addListener(LinkPersonAddressContact.class, entityListener);
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
				StructuredSelection ssel = (StructuredSelection) PersonFormEditorContentOutlinePage.this
						.getTreeViewer().getSelection();
				Object object = ssel.getFirstElement();
				if (object instanceof BankAccountRoot)
				{
					manager.add(PersonFormEditorContentOutlinePage.this.createAddBankAccountAction((BankAccountRoot) object));
				}
				else if (object instanceof BankAccount)
				{
					manager.add(PersonFormEditorContentOutlinePage.this.createEditBankAccountAction((BankAccount) object));
				}
				else if (object instanceof LinkPersonAddress)
				{
					manager.add(PersonFormEditorContentOutlinePage.this.createEditLinkAction((LinkPersonAddress) object));
				}
				if (object instanceof ContactRoot)
				{
					manager.add(PersonFormEditorContentOutlinePage.this.createAddContactAction((ContactRoot) object));
				}
				else if (object instanceof Contact)
				{
					manager.add(PersonFormEditorContentOutlinePage.this.createEditContactAction((Contact) object));
					manager.add(PersonFormEditorContentOutlinePage.this.createDeleteContactAction((Contact) object));
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

		this.setInput(this.currentPage);
	}

	private Action createEditLinkAction(final LinkPersonAddress link)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				PersonFormEditorContentOutlinePage.this.openEditor();
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
				Person person = null;
				if (currentPage instanceof FormEditorPersonPage)
				{
					person = ((FormEditorPersonPage) currentPage).getPerson();
				}
				else if (currentPage instanceof FormEditorLinkPage)
				{
					person = ((FormEditorLinkPage) currentPage).getLink().getPerson();
				}
				if (person != null)
				{
					BankAccount account = BankAccount.newInstance(person);
					Shell shell = PersonFormEditorContentOutlinePage.this.getSite().getShell();
					BankAccountDialog dialog = new BankAccountDialog(shell, account);
					dialog.open();
				}
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
				Contact contact = null;
				if (currentPage instanceof FormEditorPersonPage)
				{
					Person person = ((FormEditorPersonPage) currentPage).getPerson();
					if (person != null)
					{
						contact = PersonContact.newInstance(person);
					}
				}
				else if (currentPage instanceof FormEditorLinkPage)
				{
					LinkPersonAddress link = ((FormEditorLinkPage) currentPage).getLink();
					if (link != null)
					{
						contact = LinkPersonAddressContact.newInstance(link);
					}
				}
				if (contact != null)
				{
					Shell shell = PersonFormEditorContentOutlinePage.this.getSite().getShell();
					ContactDialog dialog = new ContactDialog(shell, contact);
					dialog.open();
				}
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
				Shell shell = PersonFormEditorContentOutlinePage.this.getSite().getShell();
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
				Shell shell = PersonFormEditorContentOutlinePage.this.getSite().getShell();
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
				Shell shell = PersonFormEditorContentOutlinePage.this.getSite().getShell();
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
		EntityMediator.removeListener(PersonContact.class, entityListener);
		EntityMediator.removeListener(LinkPersonAddressContact.class, entityListener);
		if (this.currentPage != null)
		{
			this.currentPage.getEditor().removePageChangedListener(this);
		}
		super.dispose();
	}

	private void openEditor()
	{
		StructuredSelection ssel = (StructuredSelection) PersonFormEditorContentOutlinePage.this.getTreeViewer()
				.getSelection();
		if (ssel.getFirstElement() instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new PersonEditorInput(link.getPerson()), PersonFormEditor.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void pageChanged(final PageChangedEvent event)
	{
		setInput(event.getSelectedPage());
	}

	private void setInput(final Object page)
	{
		if (page instanceof FormEditorPersonPage)
		{
			currentPage = (FormEditorPersonPage) page;
		}
		else if (page instanceof FormEditorLinkPage)
		{
			currentPage = (FormEditorLinkPage) page;
		}
		getTreeViewer().setInput(this.currentPage);
	}

	private class AddressGroupMemberRoot implements Root
	{
		private final FormPage currentPage;

		private Map<Long, AddressGroupCategoryContainer> map;

		public AddressGroupMemberRoot(final FormPage page)
		{
			this.currentPage = page;
			this.map = new HashMap<Long, AddressGroupCategoryContainer>();
		}

		@Override
		public Object[] getChildren()
		{
			if (this.map.isEmpty())
			{
				this.map.clear();
				List<AddressGroupMember> members = null;
				if (this.currentPage instanceof FormEditorPersonPage)
				{
					FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
					Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
					members = person.getAddressGroupMembers();
				}
				else if (this.currentPage instanceof FormEditorLinkPage)
				{
					FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
					LinkPersonAddress link = page.getLink();
					members = link.getAddressGroupMembers();
				}
				if (members != null && !members.isEmpty())
				{
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
				}
			}
			return map.values().toArray(new AddressGroupCategoryContainer[0]);
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
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
		private final FormPage currentPage;

		public AddressRoot(final FormPage page)
		{
			this.currentPage = page;
		}

		@Override
		public Object[] getChildren()
		{
			List<LinkPersonAddress> others = new ArrayList<LinkPersonAddress>();
			if (currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) currentPage;
				List<LinkPersonAddress> links = page.getLink().getAddress().getPersonLinks();
				for (LinkPersonAddress link : links)
				{
					if (!link.isDeleted() && !link.getPerson().isDeleted() && page.getLink().getId() != null
							&& !page.getLink().getId().equals(link.getId()))
					{
						others.add(link);
					}
				}
			}
			return others.toArray(new LinkPersonAddress[0]);
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
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
			int count = 0;
			if (currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) currentPage;
				List<LinkPersonAddress> links = page.getLink().getAddress().getPersonLinks();
				for (LinkPersonAddress link : links)
				{
					if (!link.isDeleted() && !link.getPerson().isDeleted() && page.getLink().getId() != null
							&& !page.getLink().getId().equals(link.getId()))
					{
						count++;
					}
				}
			}
			return count > 0;
		}
	}

	private class DonationRoot implements Root
	{
		private final FormPage currentPage;

		public DonationRoot(final FormPage page)
		{
			this.currentPage = page;
		}

		@Override
		public Object[] getChildren()
		{
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return person.getDonations().toArray(new Donation[0]);
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return link.getValidDonations().toArray(new Donation[0]);
			}
			return new Donation[0];
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
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
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return !person.getDonations().isEmpty();
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return !link.getDonations().isEmpty();
			}
			return false;
		}
	}

	private class ContactRoot implements Root
	{
		private final FormPage currentPage;

		public ContactRoot(final FormPage page)
		{
			this.currentPage = page;
		}

		@Override
		public Object[] getChildren()
		{
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return person.getValidContacts().toArray(new PersonContact[0]);
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return link.getValidContacts().toArray(new LinkPersonAddressContact[0]);
			}
			return new LinkPersonAddressContact[0];
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
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
			return Integer.valueOf(Order.DONATION.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return !person.getContacts().isEmpty();
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return !link.getContacts().isEmpty();
			}
			return false;
		}
	}

	private class MemberRoot implements Root
	{
		private final FormPage currentPage;

		public MemberRoot(final FormPage page)
		{
			this.currentPage = page;
		}

		@Override
		public Object[] getChildren()
		{
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return person.getMembers().toArray(new Member[0]);
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return link.getValidMembers().toArray(new Member[0]);
			}
			return new Member[0];
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
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
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return !person.getMembers().isEmpty();
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return !link.getMembers().isEmpty();
			}
			return false;
		}
	}

	public enum Order
	{
		PERSONS, BANK_ACCOUNT, COURSES, DONATION, MEMBER, ADDRESS_GROUPS;
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
			if (parentElement instanceof FormEditorPersonPage)
			{
				/*
				 * Es wurde der PersonenEditor ausgwählt
				 */
				FormEditorPersonPage page = (FormEditorPersonPage) parentElement;
				List<Root> roots = new ArrayList<Root>();
				roots.add(new MemberRoot(page));
				roots.add(new ParticipantRoot(page));
				roots.add(new DonationRoot(page));
				roots.add(new AddressGroupMemberRoot(page));
				roots.add(new BankAccountRoot(page));
				roots.add(new ContactRoot(page));
				return roots.toArray(new Root[0]);
			}
			else if (parentElement instanceof FormEditorLinkPage)
			{
				/*
				 * Es wurde ein AdressEditor ausgewählt
				 */
				FormEditorLinkPage page = (FormEditorLinkPage) parentElement;
				List<Root> roots = new ArrayList<Root>();
				roots.add(new AddressRoot(page));
				roots.add(new MemberRoot(page));
				roots.add(new ParticipantRoot(page));
				roots.add(new DonationRoot(page));
				roots.add(new AddressGroupMemberRoot(page));
				roots.add(new BankAccountRoot(page));
				roots.add(new ContactRoot(page));
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

	private class ParticipantRoot implements Root
	{
		private final FormPage currentPage;

		public ParticipantRoot(final FormPage page)
		{
			this.currentPage = page;
		}

		@Override
		public Object[] getChildren()
		{
			return getParticipants().toArray(new Participant[0]);
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_PARTICIPANT);
		}

		@Override
		public String getName()
		{
			return "Besuchte Kurse (" + getParticipants().size() + ")";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.COURSES.ordinal());
		}

		private List<Participant> getParticipants()
		{
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return person.getParticipants();
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return link.getValidParticipants();
			}
			return new ArrayList<Participant>();
		}

		@Override
		public boolean hasChildren()
		{
			return getParticipants().size() > 0;
		}
	}

	private class BankAccountRoot implements Root
	{
		private final FormPage currentPage;

		public BankAccountRoot(final FormPage page)
		{
			this.currentPage = page;
		}

		@Override
		public Object[] getChildren()
		{
			return getBankAccounts().toArray(new BankAccount[0]);
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
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
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return person.getValidBankAccounts();
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				return page.getLink().getPerson().getValidBankAccounts();
			}
			return new ArrayList<BankAccount>();
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

		FormPage getFormPage();

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
}
