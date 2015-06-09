package ch.eugster.events.addressgroup.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.dialogs.AddressGroupMemberTreeContentProvider;
import ch.eugster.events.addressgroup.dialogs.AddressGroupMemberTreeLabelProvider;
import ch.eugster.events.domain.views.DomainLabelProvider;
import ch.eugster.events.domain.views.DomainSorter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.AddressGroupMemberQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.views.AbstractEntityView;

public class PersonAddressGroupMemberView extends AbstractEntityView implements ISelectionChangedListener,
		ITreeViewerListener, ICheckStateListener, ISelectionListener
{
	public static final String ID = "ch.eugster.events.addressgroup.person.view";

	private AbstractEntity parent;

	private ComboViewer domainViewer;

	private ContainerCheckedTreeViewer addressGroupViewer;

	private final Map<Long, Monitor> monitors = new HashMap<Long, Monitor>();

	private final Map<Long, AddressGroupMember> current = new HashMap<Long, AddressGroupMember>();

	// private final String message =
	// "Selektieren oder Deselektieren Sie die Adressgruppen und speichern Sie die Änderungen.\nSie können die Änderungen jederzeit mit 'Abbrechen' widerrufen.";

	private ServiceTracker connectionServiceTracker;

	private boolean dirty;

	private void checkCategory(final Object object)
	{
		if (object instanceof Domain)
		{
			Domain domain = (Domain) object;
			Object[] elements = ((ITreeContentProvider) this.addressGroupViewer.getContentProvider())
					.getElements(domain);
			for (Object element : elements)
			{
				if (element instanceof AddressGroupCategory)
				{
					AddressGroupCategory category = (AddressGroupCategory) element;
					List<AddressGroup> addressGroups = category.getAddressGroups();
					int checked = 0;
					for (AddressGroup addressGroup : addressGroups)
					{
						Monitor monitor = this.monitors.get(addressGroup.getId());
						if (monitor != null)
						{
							if (monitor.checked)
							{
								checked++;
							}
						}
					}
					if (checked == addressGroups.size())
					{
						this.addressGroupViewer.setChecked(category, true);
					}
					else if (checked > 0)
					{
						this.addressGroupViewer.setGrayChecked(category, true);
					}
				}
			}
		}
	}

	@Override
	public void checkStateChanged(final CheckStateChangedEvent event)
	{
		Object object = event.getElement();
		if (object instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) object;
			List<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				this.updateMonitor(addressGroup, event.getChecked());
			}
		}
		else if (object instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) object;
			// if (event.getChecked())
			// {
			// for (AddressGroupMember member :
			// addressGroup.getAddressGroupMembers())
			// {
			// Class<?> clazz = parent.getClass();
			// if (clazz.equals(Address.class))
			// {
			// if (member.getAddress().getId().equals(parent.getId()))
			// {
			// this.current.put(addressGroup.getId(), member);
			// break;
			// }
			// }
			// else if (clazz.equals(LinkPersonAddress.class))
			// {
			// if (member.getLink() != null &&
			// member.getLink().getId().equals(parent.getId()))
			// {
			// this.current.put(addressGroup.getId(), member);
			// break;
			// }
			// }
			// }
			// }
			// if (event.getChecked())
			// {
			// updateCurrent(addressGroup);
			// }
			this.updateMonitor(addressGroup, event.getChecked());
		}
		setDirty(true);
	}

	// private void updateCurrent(AddressGroup addressGroup)
	// {
	// ConnectionService service = (ConnectionService)
	// connectionServiceTracker.getService();
	// if (service != null)
	// {
	// List<AddressGroupMember> members = new
	// ArrayList<AddressGroupMember>();
	// if (this.parent instanceof Address)
	// {
	// Address address = (Address) this.parent;
	// AddressGroupMemberQuery query = (AddressGroupMemberQuery)
	// service.getQuery(AddressGroupMember.class);
	// members = query.selectByAddressAndAddressGroup(address, addressGroup);
	// }
	// else if (this.parent instanceof LinkPersonAddress)
	// {
	// LinkPersonAddress link = (LinkPersonAddress) this.parent;
	// AddressGroupMemberQuery query = (AddressGroupMemberQuery)
	// service.getQuery(AddressGroupMember.class);
	// members = query.selectByLinkPersonAddressAndAddressGroup(link,
	// addressGroup);
	// }
	// for (AddressGroupMember member : members)
	// {
	// Class<?> clazz = parent.getClass();
	// if (clazz.equals(Address.class))
	// {
	// if (member.getAddress().getId().equals(parent.getId()))
	// {
	// this.current.put(addressGroup.getId(), member);
	// break;
	// }
	// }
	// else if (clazz.equals(LinkPersonAddress.class))
	// {
	// if (member.getLink() != null &&
	// member.getLink().getId().equals(parent.getId()))
	// {
	// this.current.put(addressGroup.getId(), member);
	// break;
	// }
	// }
	// }
	// }
	// }

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginWidth = 0;

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.domainViewer = new ComboViewer(combo);
		this.domainViewer.setContentProvider(new ArrayContentProvider());
		this.domainViewer.setLabelProvider(new DomainLabelProvider());
		this.domainViewer.setSorter(new DomainSorter());
		this.domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });

		final Tree tree = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.CHECK | SWT.HIDE_SELECTION);
		tree.setHeaderVisible(false);
		tree.setLinesVisible(false);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(final Event event)
			{
				if (event.detail == SWT.CHECK)
				{
					if (!(event.item.getData() instanceof AddressGroup))
					{
						event.detail = SWT.NONE;
						event.type = SWT.None;
						event.doit = false;
						try
						{
							tree.setRedraw(false);
							TreeItem item = (TreeItem) event.item;
							item.setChecked(!item.getChecked());
						}
						finally
						{
							tree.setRedraw(true);
						}
					}
				}
			}
		});

		this.addressGroupViewer = new ContainerCheckedTreeViewer(tree);
		this.addressGroupViewer.setContentProvider(new AddressGroupMemberTreeContentProvider());
		this.addressGroupViewer.setLabelProvider(new AddressGroupMemberTreeLabelProvider());
		this.addressGroupViewer.setSorter(new PersonAddressGroupMemberSorter(this));
		this.addressGroupViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.addressGroupViewer.addCheckStateListener(this);
		this.addressGroupViewer.addTreeListener(this);

		this.domainViewer.addSelectionChangedListener(this);

		PersonView view = (PersonView) this.getSite().getPage().findView(PersonView.ID);
		if (view != null)
		{
			IStructuredSelection ssel = (IStructuredSelection) view.getViewer().getSelection();
			Object object = ssel.getFirstElement();
			if (object instanceof Person)
			{
				Person person = (Person) object;
				this.parent = person.getDefaultLink();
			}
			else if (object instanceof LinkPersonAddress || object instanceof Address)
			{
				this.parent = (AbstractEntity) object;
			}
			reset();
		}

		this.getSite().getPage().addSelectionListener(PersonView.ID, this);

	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();

		EntityMediator.removeListener(AddressGroupMember.class, this);
		EntityMediator.removeListener(AddressGroup.class, this);
		EntityMediator.removeListener(LinkPersonAddress.class, this);
		EntityMediator.removeListener(Person.class, this);
		EntityMediator.removeListener(Address.class, this);

		this.getSite().getPage().removeSelectionListener("ch.eugster.events.person.view", this);

		super.dispose();
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		EntityMediator.addListener(AddressGroupMember.class, this);
		EntityMediator.addListener(AddressGroup.class, this);
		EntityMediator.addListener(LinkPersonAddress.class, this);
		EntityMediator.addListener(Address.class, this);
		EntityMediator.addListener(Person.class, this);

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		connectionServiceTracker.open();

		this.setPartName("Adressgruppen");
	}

	public boolean isChecked(final AddressGroup addressGroup)
	{
		Monitor monitor = monitors.get(addressGroup.getId());
		return (monitor == null) ? false : monitor.checked;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void reset()
	{
		ConnectionService service = (ConnectionService) connectionServiceTracker.getService();
		if (service != null)
		{
			this.monitors.clear();
			this.current.clear();

			List<AddressGroupMember> members = null;

			if (this.parent instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.parent;
				AddressGroupMemberQuery query = (AddressGroupMemberQuery) service.getQuery(AddressGroupMember.class);
				members = query.selectByLink(link);
				List<AddressGroupMember> addressMembers = link.getAddress().getAddressAddressGroupMembers();
				members.addAll(addressMembers);
			}
			else if (this.parent instanceof Address)
			{
				AddressGroupMemberQuery query = (AddressGroupMemberQuery) service.getQuery(AddressGroupMember.class);
				members = query.selectByAddress((Address) this.parent);
			}
			if (members != null)
			{
				for (AddressGroupMember member : members)
				{
					Monitor monitor = this.monitors.get(member.getAddressGroup().getId());
					if (monitor == null)
					{
						monitor = new Monitor(member.getAddressGroup(), !member.isDeleted());
						this.monitors.put(member.getAddressGroup().getId(), monitor);
						this.current.put(member.getAddressGroup().getId(), member);
					}
					else if (!monitor.checked)
					{
						monitor.checked = !member.isDeleted();
					}
				}
			}

			List<Domain> domains = new ArrayList<Domain>();
			DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
			domains = query.selectValids();
			domainViewer.setInput(domains.toArray(new Domain[0]));

			Domain domain = Domain.newInstance();
			if (this.parent instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.parent;
				if (link.getPerson().getDomain() != null)
				{
					domain = link.getPerson().getDomain();
				}
			}
			else
			{
				if (User.getCurrent() != null)
				{
					if (User.getCurrent().getDomain() != null)
					{
						domain = User.getCurrent().getDomain();
					}
				}
			}

			this.domainViewer.setSelection(new StructuredSelection(domain));
			this.checkCategory(domain);
		}
		this.setDirty(false);
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
	{
		if (selection instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) selection;
			if (ssel.getFirstElement() instanceof AbstractEntity)
			{
				if (ssel.getFirstElement() instanceof Person)
				{
					Person person = (Person) ssel.getFirstElement();
					this.parent = person.getDefaultLink();
				}
				else if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{
					this.parent = (LinkPersonAddress) ssel.getFirstElement();
				}
				else if (ssel.getFirstElement() instanceof Address)
				{
					this.parent = (Address) ssel.getFirstElement();
				}
			}
			else
			{
				this.parent = null;
			}
			reset();
		}
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (event.getSource().equals(this.domainViewer))
		{
			ComboViewer comboViewer = (ComboViewer) event.getSource();
			StructuredSelection ssel = (StructuredSelection) comboViewer.getSelection();
			if (ssel.isEmpty())
			{
				updateViewer(Domain.newInstance());
			}
			else
			{
				updateViewer(ssel.getFirstElement());
				this.checkCategory(ssel.getFirstElement());
			}
		}
	}

	private void setDirty(final boolean dirty)
	{
		this.dirty = dirty;
		this.setPartName(dirty ? "*Adressgruppen" : "Adressgruppen");
		this.firePartPropertyChanged("dirty", null, Boolean.valueOf(dirty).toString());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		this.addressGroupViewer.getControl().setFocus();
	}

	@Override
	public void treeCollapsed(final TreeExpansionEvent event)
	{
	}

	@Override
	public void treeExpanded(final TreeExpansionEvent event)
	{
		if (event.getElement() instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = ((AddressGroupCategory) event.getElement());
			List<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				Monitor monitor = this.monitors.get(addressGroup.getId());
				if (monitor != null)
				{
					this.addressGroupViewer.setChecked(addressGroup, monitor.checked);
				}
			}
		}
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		refreshViewer();
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		refreshViewer();
	}

	@Override
	public void postRemove(AbstractEntity entity)
	{
		refreshViewer();
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		refreshViewer(entity);
	}

	public void updateAddressGroupMembers()
	{
		Long[] addressGroupIds = this.monitors.keySet().toArray(new Long[0]);
		for (Long addressGroupId : addressGroupIds)
		{
			ConnectionService service = (ConnectionService) connectionServiceTracker.getService();
			if (service != null)
			{
				AddressGroupMember member = this.current.get(addressGroupId);
				Monitor monitor = this.monitors.get(addressGroupId);
				if (member == null)
				{
					if (monitor.checked)
					{
						if (parent instanceof LinkPersonAddress)
						{
							member = getMember(monitor.addressGroup, (LinkPersonAddress) parent);
							if (member == null)
							{
								member = AddressGroupMember.newInstance(monitor.addressGroup,
										(LinkPersonAddress) this.parent);
							}
						}
						else if (parent instanceof Address)
						{
							member = getMember(monitor.addressGroup, (Address) parent);
							if (member == null)
							{
								member = AddressGroupMember.newInstance(monitor.addressGroup, (Address) this.parent);
							}
						}
						if (member != null)
						{
							member.setDeleted(false);
							AddressGroupMemberQuery query = (AddressGroupMemberQuery) service
									.getQuery(AddressGroupMember.class);
							current.put(addressGroupId, query.merge(member));
						}
					}
				}
				else
				{
					if (monitor.checked == member.isDeleted())
					{
						member.setDeleted(!monitor.checked);
						AddressGroupMemberQuery query = (AddressGroupMemberQuery) service
								.getQuery(AddressGroupMember.class);
						current.put(addressGroupId, query.merge(member));
					}
				}
			}
		}
		this.setDirty(false);
	}
	
	private AddressGroupMember getMember(AddressGroup addressGroup, LinkPersonAddress link)
	{
		List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (member.getLink() != null && member.getLink().getId().equals(link.getId()))
			{
				return member;
			}
		}
		return null;
	}

	private AddressGroupMember getMember(AddressGroup addressGroup, Address address)
	{
		List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (member.getAddress().getId().equals(address.getId()))
			{
				return member;
			}
		}
		return null;
	}

	private void internalRefresh()
	{
		if (!addressGroupViewer.getControl().isDisposed())
		{
			addressGroupViewer.refresh();
			checkCategory(addressGroupViewer.getInput());
		}
	}

	private void internalRefresh(Object object)
	{
		if (!addressGroupViewer.getControl().isDisposed())
		{
			addressGroupViewer.refresh(object);
			checkCategory(object);
		}
	}

	private void refreshViewer()
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				internalRefresh();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void refreshViewer(final Object object)
	{
		UIJob job = new UIJob("")
		{

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				internalRefresh(object);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void updateViewer(final Object object)
	{
		UIJob updateViewer = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (!addressGroupViewer.getControl().isDisposed())
				{
					PersonAddressGroupMemberView.this.addressGroupViewer.getControl().setEnabled(false);
					PersonAddressGroupMemberView.this.showBusy(true);
					PersonAddressGroupMemberView.this.addressGroupViewer.setInput(object);
					PersonAddressGroupMemberView.this.internalRefresh();
					PersonAddressGroupMemberView.this.showBusy(false);
					PersonAddressGroupMemberView.this.addressGroupViewer.getControl().setEnabled(true);
				}
				return Status.OK_STATUS;
			}

		};
		updateViewer.setUser(true);
		updateViewer.schedule();
	}

	private void updateMonitor(final AddressGroup addressGroup, final boolean checked)
	{
		Monitor monitor = this.monitors.get(addressGroup.getId());
		if (monitor == null)
		{
			monitor = new Monitor(addressGroup, checked);
			this.monitors.put(addressGroup.getId(), monitor);
		}
		monitor.checked = checked;
	}

	// private class AddressGroupContainerCheckedTreeViewer extends
	// ContainerCheckedTreeViewer
	// {
	//
	// public AddressGroupContainerCheckedTreeViewer(final Tree tree)
	// {
	// super(tree);
	// }
	//
	// /*
	// * (non-Javadoc) Method declared on StructuredViewer.
	// */
	// @Override
	// protected void handleSelect(final SelectionEvent event)
	// {
	// if (event.detail == SWT.CHECK)
	// {
	// TreeItem item = (TreeItem) event.item;
	// Object data = item.getData();
	// if (data instanceof AddressGroup)
	// {
	// super.handleSelect(event);
	// if (data != null)
	// {
	// fireCheckStateChanged(new CheckStateChangedEvent(this, data,
	// item.getChecked()));
	// }
	// }
	// else
	// {
	// event.doit = false;
	// }
	// }
	// else
	// {
	// super.handleSelect(event);
	// }
	// }
	//
	// }

	private class Monitor
	{
		// public boolean update = false;

		public boolean checked = false;

		public AddressGroup addressGroup = null;

		public Monitor(final AddressGroup addressGroup, final boolean checked)
		{
			this.addressGroup = addressGroup;
			this.checked = checked;
		}
	}
}