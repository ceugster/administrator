package ch.eugster.events.addressgroup.dialogs;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
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
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.domain.views.DomainLabelProvider;
import ch.eugster.events.domain.views.DomainSorter;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.AddressGroupMemberQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupMemberDialog extends TitleAreaDialog implements ISelectionChangedListener,
		ITreeViewerListener, ICheckStateListener
{
	private final AbstractEntity parent;

	private ComboViewer domainViewer;

	private ContainerCheckedTreeViewer addressGroupViewer;

	private final Hashtable<Long, Monitor> monitors = new Hashtable<Long, Monitor>();

	private final Hashtable<Long, AddressGroupMember> current = new Hashtable<Long, AddressGroupMember>();

	private final String message = "Selektieren oder Deselektieren Sie die Adressgruppen und speichern Sie die Änderungen.\nSie können die Änderungen jederzeit mit 'Abbrechen' widerrufen.";

	private boolean isPageComplete = false;

	public AddressGroupMemberDialog(final Shell parentShell, final Address address)
	{
		super(parentShell);
		this.setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
		this.parent = address;
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
		this.setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
		this.parent = link;
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
					Collection<AddressGroup> addressGroups = category.getAddressGroups();
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
			Collection<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				this.updateMonitor(addressGroup, event.getChecked());
			}
		}
		else if (object instanceof AddressGroup)
		{
			this.updateMonitor((AddressGroup) object, event.getChecked());
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Speichern", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			Collection<AddressGroupMember> members = null;
			AddressGroupMemberQuery query = (AddressGroupMemberQuery) service.getQuery(AddressGroupMember.class);
			if (this.parent instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.parent;
				members = query.selectByLink(link);
				Collection<AddressGroupMember> addressMembers = link.getAddress().getAddressAddressGroupMembers();
				members.addAll(addressMembers);
			}
			else if (this.parent instanceof Address)
			{
				members = query.selectByAddress((Address) this.parent);
			}
			if (members != null)
			{
				for (AddressGroupMember member : members)
				{
					member = (AddressGroupMember) query.refresh(member);
					if (member.getAddressGroup() != null)
					{
						Monitor monitor = new Monitor(member.getAddressGroup(), !member.isDeleted());
						monitor.checked = !member.isDeleted();
						this.monitors.put(member.getAddressGroup().getId(), monitor);
						this.current.put(member.getAddressGroup().getId(), member);
					}
				}
			}
		}

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
		this.addressGroupViewer.setSorter(new AddressGroupMemberSorter(this));
		this.addressGroupViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.addressGroupViewer.addCheckStateListener(this);
		this.addressGroupViewer.addTreeListener(this);

		if (service != null)
		{
			DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
			List<Domain> domains = query.selectAll();
			domainViewer.setInput(domains.toArray(new Domain[0]));
		}
		this.domainViewer.addSelectionChangedListener(this);

		tracker.close();

		Domain domain = null;
		if (this.parent instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) this.parent;
			if (link.getPerson().getDomain() != null)
			{
				domain = link.getPerson().getDomain();
			}
		}
		if (domain == null)
		{
			if (User.getCurrent() != null)
			{
				if (User.getCurrent().getDomain() != null)
				{
					domain = User.getCurrent().getDomain();
				}
			}
		}
		if (domain != null)
		{
			this.domainViewer.setSelection(new StructuredSelection(domain));
		}

		return parent;
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
		this.updateAddressGroupMembers();
		super.okPressed();
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
				this.addressGroupViewer.setInput(null);
			}
			else
			{
				this.addressGroupViewer.setInput(ssel.getFirstElement());
				this.checkCategory(ssel.getFirstElement());
			}
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
			Collection<AddressGroup> addressGroups = category.getAddressGroups();
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
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
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
								member.setInserted(GregorianCalendar.getInstance());
							}
						}
						else if (parent instanceof Address)
						{
							member = getMember(monitor.addressGroup, (Address) parent);
							if (member == null)
							{
								member = AddressGroupMember.newInstance(monitor.addressGroup, (Address) this.parent);
								member.setInserted(GregorianCalendar.getInstance());
							}
						}
						if (member != null)
						{
							member.setDeleted(false);
							AddressGroupMemberQuery query = (AddressGroupMemberQuery) service
									.getQuery(AddressGroupMember.class);
							current.put(addressGroupId, query.merge(member));
							AddressGroup addressGroup = (AddressGroup) service.refresh(member.getAddressGroup());
							this.addressGroupViewer.refresh(addressGroup);
						}
					}
				}
				else
				{
					if (monitor.checked == member.isDeleted())
					{
						member.setDeleted(!monitor.checked);
						member.setUpdated(GregorianCalendar.getInstance());
						if (parent instanceof LinkPersonAddress)
						{
							member.setParent((LinkPersonAddress) parent, member.getAddress());
						}
						else if (parent instanceof Address)
						{
							member.setParent(null, (Address) parent);
						}
						AddressGroupMemberQuery query = (AddressGroupMemberQuery) service
								.getQuery(AddressGroupMember.class);
						current.put(addressGroupId, query.merge(member));
					}
				}
			}
			tracker.close();
		}
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

	private void updateMonitor(final AddressGroup addressGroup, final boolean checked)
	{
		Monitor monitor = this.monitors.get(addressGroup.getId());
		if (monitor == null)
		{
			monitor = new Monitor(addressGroup);
			this.monitors.put(addressGroup.getId(), monitor);
		}
		monitor.checked = checked;
	}

	private class Monitor
	{
		public boolean checked = false;

		public AddressGroup addressGroup = null;

		public Monitor(final AddressGroup addressGroup)
		{
			this.addressGroup = addressGroup;
		}

		public Monitor(final AddressGroup addressGroup, final boolean checked)
		{
			this.addressGroup = addressGroup;
			this.checked = checked;
		}
	}
}
