package ch.eugster.events.person.views;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IPageChangedListener;
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

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.BookingParticipantFilter;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroup;
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
import ch.eugster.events.person.dialogs.BankAccountDialog;
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

	private final EntityAdapter bankAccountListener;

	public PersonFormEditorContentOutlinePage(final FormPage page)
	{
		this.currentPage = page;
		this.currentPage.getEditor().addPageChangedListener(this);
		this.bankAccountListener = new EntityAdapter() 
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
			}
		};
		EntityMediator.addListener(BankAccount.class, bankAccountListener);
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
//					account.getPropertyChangeSupport().addPropertyChangeListener(PersonFormEditorContentOutlinePage.this);
					Shell shell = PersonFormEditorContentOutlinePage.this.getSite().getShell();
					BankAccountDialog dialog = new BankAccountDialog(shell, account);
					dialog.open();
//					account.getPropertyChangeSupport().removePropertyChangeListener(PersonFormEditorContentOutlinePage.this);
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
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("ADD"));
		return action;
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(BankAccount.class, bankAccountListener);
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

		public AddressGroupMemberRoot(final FormPage page)
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
				return person.getAddressGroupMembers().toArray(new AddressGroupMember[0]);
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return link.getValidAddressGroupMembers().toArray(new AddressGroupMember[0]);
			}
			return new AddressGroupMember[0];
		}

		@Override
		public FormPage getFormPage()
		{
			return currentPage;
		}

		@Override
		public Image getImage()
		{
			return Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE);
		}

		@Override
		public String getName()
		{
			return "Adressgruppen (" + getChildren().length + ")";
		}

		@Override
		public Integer getOrder()
		{
			return Integer.valueOf(Order.ADDRESS_GROUPS.ordinal());
		}

		@Override
		public boolean hasChildren()
		{
			if (this.currentPage instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage page = (FormEditorPersonPage) this.currentPage;
				Person person = ((PersonEditorInput) page.getEditor().getEditorInput()).getEntity();
				return person.getAddressGroupMembers().size() > 0;
			}
			else if (this.currentPage instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage page = (FormEditorLinkPage) this.currentPage;
				LinkPersonAddress link = page.getLink();
				return link.getAddressGroupMembers().size() > 0;
			}
			return false;
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
				AddressGroup group = member.getAddressGroup();
				return group.getCode().isEmpty() ? group.getName() : (group.getCode() + (group.getName().isEmpty() ? ""
						: " - " + group.getName()));
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
}
