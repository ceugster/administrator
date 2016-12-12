package ch.eugster.events.addressgroup.dialogs;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.AddressGroupMemberSelector;
import ch.eugster.events.addressgroup.Monitor;
import ch.eugster.events.addressgroup.views.AddressGroupMemberTreeSorter;
import ch.eugster.events.addressgroup.views.PersonAddressGroupMemberTreeContentProvider;
import ch.eugster.events.addressgroup.views.PersonAddressGroupMemberTreeLabelProvider;
import ch.eugster.events.domain.views.DomainLabelProvider;
import ch.eugster.events.domain.views.DomainSorter;
import ch.eugster.events.persistence.events.EntityAdapter;
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

public class AddressGroupMemberDialog extends TitleAreaDialog implements ISelectionChangedListener,
		ITreeViewerListener, ICheckStateListener, AddressGroupMemberSelector
{
	private ContainerCheckedTreeViewer addressGroupViewer;

	private final Hashtable<Long, AddressGroupMember> current = new Hashtable<Long, AddressGroupMember>();

	private ComboViewer domainViewer;

	private boolean dirty;
	
	private boolean isPageComplete = false;
	
	private final String message = "Selektieren oder Deselektieren Sie die Adressgruppen und speichern Sie die Änderungen.\nSie können die Änderungen jederzeit mit 'Abbrechen' widerrufen.";

	private final Hashtable<Long, Monitor> monitors = new Hashtable<Long, Monitor>();

	private AbstractEntity parent;

	private ServiceTracker<ConnectionService, ConnectionService> tracker;

	private ConnectionService connectionService;

	private EntityAdapter adapter;
	
	public AddressGroupMemberDialog(final Shell parentShell, final Address address)
	{
		super(parentShell);
		init(address);
	}

	/**
	 * @param parentShell
	 * @param parent
	 *            <code>parent</code> must be of type
	 *            ch.eugster.events.data.objects.Customer
	 * @param addressGroup
	 *            Falls eine neue Adressgruppe erfasst wird, muss diese bereit
	 *            vor der Übergabe an den Konstruktor von
	 *            <code>AddressGroupDialog</code> instantiiert sein und der
	 *            Parent <code>Domain</code> muss - falls ein solcher gesetzt
	 *            werden soll, ebenfalls dem Konstruktur von
	 *            <code>AddressGroup</code> übergeben worden sein.
	 * 
	 */
	public AddressGroupMemberDialog(final Shell parentShell, final LinkPersonAddress link)
	{
		super(parentShell);
		init(link);
	}

	private void init(AbstractEntity entity)
	{
		this.setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
		this.parent = entity;
		
		adapter = new EntityAdapter() 
		{
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
			public void postUpdate(AbstractEntity entity) 
			{
				refreshViewer(entity);
			}
		};
		 
		EntityMediator.addListener(AddressGroupMember.class, adapter);
		EntityMediator.addListener(AddressGroup.class, adapter);
		EntityMediator.addListener(LinkPersonAddress.class, adapter);
		EntityMediator.addListener(Address.class, adapter);
		EntityMediator.addListener(Person.class, adapter);

		tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		connectionService = (ConnectionService) tracker.getService();
	}
	
	@Override
	public boolean close() 
	{
		tracker.close();
		
		EntityMediator.removeListener(AddressGroupMember.class, adapter);
		EntityMediator.removeListener(AddressGroup.class, adapter);
		EntityMediator.removeListener(LinkPersonAddress.class, adapter);
		EntityMediator.removeListener(Address.class, adapter);
		EntityMediator.removeListener(Person.class, adapter);
		
		return super.close();
	}

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
					List<AddressGroup> addressGroups = category.getValidAddressGroups();
					int checked = 0;
					for (AddressGroup addressGroup : addressGroups)
					{
						Monitor monitor = this.monitors.get(addressGroup.getId());
						if (monitor != null && monitor.isValid())
						{
							if (monitor.checked)
							{
								checked++;
							}
						}
						this.addressGroupViewer.setChecked(addressGroup, monitor != null &&  monitor.isValid() && monitor.checked);
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
			this.updateMonitor(addressGroup, event.getChecked());
		}
		setDirty(true);
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Speichern", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	private void setDirty(final boolean dirty)
	{
		this.dirty = dirty;
	}
	
	private boolean isDirty()
	{
		return this.dirty;
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

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
		this.addressGroupViewer.setContentProvider(new PersonAddressGroupMemberTreeContentProvider());
		this.addressGroupViewer.setLabelProvider(new PersonAddressGroupMemberTreeLabelProvider(monitors));
		this.addressGroupViewer.setSorter(new AddressGroupMemberTreeSorter(this));
		this.addressGroupViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.addressGroupViewer.addCheckStateListener(this);
		this.addressGroupViewer.addTreeListener(this);

		if (connectionService != null)
		{
			this.domainViewer.addSelectionChangedListener(this);

			DomainQuery query = (DomainQuery) connectionService.getQuery(Domain.class);
			List<Domain> domains = query.selectValids();
			if (domains.isEmpty())
			{
				domains.add(Domain.newInstance());
			}
			domainViewer.setInput(domains.toArray(new Domain[0]));
			Domain selectedDomain = null;
			if (this.parent instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.parent;
				selectedDomain = link.getPerson().getDomain();
			}
			if (selectedDomain == null)
			{
				selectedDomain = User.getCurrent().getDomain();
			}
			if (selectedDomain != null)
			{
				domainViewer.setSelection(new StructuredSelection(new Domain[] { selectedDomain }));
			}
			reset();
		}
		return parent;
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

	public void reset()
	{
		if (connectionService != null)
		{
			this.monitors.clear();
			this.current.clear();

			List<AddressGroupMember> members = null;

			if (this.parent instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.parent;
				AddressGroupMemberQuery query = (AddressGroupMemberQuery) connectionService.getQuery(AddressGroupMember.class);
				members = query.selectByLink(link);
			}
			else if (this.parent instanceof Address)
			{
				AddressGroupMemberQuery query = (AddressGroupMemberQuery) connectionService.getQuery(AddressGroupMember.class);
				members = query.selectByAddress((Address) this.parent);
			}
			if (members != null)
			{
				for (AddressGroupMember member : members)
				{
					Monitor monitor = this.monitors.get(member.getAddressGroup().getId());
					if (monitor == null)
					{
						monitor = new Monitor(member, !member.isDeleted());
						this.monitors.put(member.getAddressGroup().getId(), monitor);
						this.current.put(member.getAddressGroup().getId(), member);
					}
					else if (!monitor.checked)
					{
						monitor.checked = !member.isDeleted();
					}
				}
			}
			IStructuredSelection ssel = (IStructuredSelection) domainViewer.getSelection();
			this.checkCategory(ssel.getFirstElement());
		}
		this.setDirty(false);
	}
	
	private AddressGroupMember getMember(AddressGroup addressGroup, Address address)
	{
		List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (member.isValidAddressMember() || member.isValidLinkMember())
			{
				if (member.getAddressId().equals(address.getId()))
				{
					return member;
				}
			}
		}
		return null;
	}

	private AddressGroupMember getMember(AddressGroup addressGroup, LinkPersonAddress link)
	{
		List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (member.isValidLinkMember() && member.getLink().getId().equals(link.getId()))
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

	public boolean isChecked(final AddressGroup addressGroup)
	{
		Monitor monitor = monitors.get(addressGroup.getId());
		return monitor == null ? false : monitor.checked;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		if (this.isDirty())
		{
			this.updateAddressGroupMembers();
		}
		super.okPressed();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (event.getSource().equals(this.domainViewer))
		{
			ComboViewer comboViewer = (ComboViewer) event.getSource();
			StructuredSelection ssel = (StructuredSelection) comboViewer.getSelection();
			this.updateViewer(ssel.getFirstElement());
			this.checkCategory(ssel.getFirstElement());
		}
	}

	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
		this.setPageComplete(false);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		super.setMessage(this.message);
		this.setPageComplete(true);
	}

	public void setPageComplete(final boolean isComplete)
	{
		this.isPageComplete = isComplete;
		if (this.getButton(IDialogConstants.OK_ID) != null)
			this.getButton(IDialogConstants.OK_ID).setEnabled(this.isPageComplete);
	}

	public void setTitle()
	{
		super.setTitle("Zuordnungen zu Adressgruppen");
		int width = this.getShell().getDisplay().getClientArea().width;
		int height = this.getShell().getDisplay().getClientArea().height;
		super.getShell().setMinimumSize(width > 500 ? 500 : width, height > 500 ? 500 : height);
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
					this.addressGroupViewer.setChecked(addressGroup, monitor.checked);
			}
		}
	}

	public void updateAddressGroupMembers()
	{
		Long[] addressGroupIds = this.monitors.keySet().toArray(new Long[0]);
		for (Long addressGroupId : addressGroupIds)
		{
			if (connectionService != null)
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
						else if (this.parent instanceof Address)
						{
							member = getMember(monitor.addressGroup, (Address) this.parent);
							if (member == null)
							{
								member = AddressGroupMember.newInstance(monitor.addressGroup, (Address) this.parent);
							}
						}
						if (member != null)
						{
							member.setDeleted(false);
							AddressGroupMemberQuery query = (AddressGroupMemberQuery) connectionService.getQuery(AddressGroupMember.class);
							member = query.merge(member);
							this.current.put(addressGroupId, member);
							connectionService.refresh(this.parent);
							monitor.addressGroupMember = member;
						}
					}
				}
				else
				{
					if (monitor.checked == member.isDeleted())
					{
						member.setDeleted(!monitor.checked);
						AddressGroupMemberQuery query = (AddressGroupMemberQuery) connectionService.getQuery(AddressGroupMember.class);
						this.current.put(addressGroupId, query.merge(member));
						connectionService.refresh(this.parent);
					}
				}
			}
		}
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

	private void updateViewer(final Object object)
	{
		UIJob updateViewer = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (!addressGroupViewer.getControl().isDisposed())
				{
					AddressGroupMemberDialog.this.addressGroupViewer.getControl().setEnabled(false);
					AddressGroupMemberDialog.this.addressGroupViewer.setInput(object);
					AddressGroupMemberDialog.this.internalRefresh();
					AddressGroupMemberDialog.this.addressGroupViewer.getControl().setEnabled(true);
				}
				return Status.OK_STATUS;
			}

		};
		updateViewer.setUser(true);
		updateViewer.schedule();
	}
	
}
