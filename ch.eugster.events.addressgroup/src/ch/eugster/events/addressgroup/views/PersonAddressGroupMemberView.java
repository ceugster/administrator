package ch.eugster.events.addressgroup.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import ch.eugster.events.addressgroup.AddressGroupMemberSelector;
import ch.eugster.events.addressgroup.Monitor;
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
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupMemberQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.views.AbstractEntityView;

public class PersonAddressGroupMemberView extends AbstractEntityView implements ISelectionChangedListener,
		ITreeViewerListener, ICheckStateListener, ISelectionListener, AddressGroupMemberSelector
{
	public static final String ID = "ch.eugster.events.addressgroup.person.view";

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private final Map<Long, AddressGroupMember> current = new HashMap<Long, AddressGroupMember>();

	private boolean dirty;

	private ContainerCheckedTreeViewer addressGroupViewer;

	private final Map<Long, Monitor> monitors = new HashMap<Long, Monitor>();

	private AbstractEntity parent;
	
	private void checkParents()
	{
		this.addressGroupViewer.expandAll();
		TreeItem[] items = this.addressGroupViewer.getTree().getItems();
		for (TreeItem item : items)
		{
			Object element = item.getData();
			if (element instanceof Domain)
			{
				this.addressGroupViewer.setSubtreeChecked(element, false);
				ConnectionService service = (ConnectionService) connectionServiceTracker.getService();
				AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) service.getQuery(AddressGroupCategory.class);
				List<AddressGroupCategory> categories = query.selectValidsByDomain((Domain)element);
				int checkedCategories = 0;
				int grayCheckedCategories = 0;
				for (AddressGroupCategory category : categories)
				{
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
						checkedCategories++;
						this.addressGroupViewer.setChecked(category, true);
					}
					else if (checked > 0)
					{
						grayCheckedCategories++;
						this.addressGroupViewer.setGrayChecked(category, true);
					}
				}
				if (checkedCategories > 0 && checkedCategories == categories.size())
				{
					this.addressGroupViewer.setChecked(element, true);
				}
				else if (grayCheckedCategories > 0)
				{
					this.addressGroupViewer.setGrayChecked(element, true);
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

		PersonView view = (PersonView) this.getSite().getPage().findView(PersonView.ID);
		if (view != null)
		{
			IStructuredSelection ssel = (IStructuredSelection) view.getViewer().getSelection();
			if (ssel.isEmpty())
			{
				this.parent = null;
			}
			else
			{
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
			}
		}
		this.addressGroupViewer.setInput(connectionServiceTracker.getService());
		reset();
		this.getSite().getPage().addSelectionListener(PersonView.ID, this);
	}
	
	@Override
	public void dispose()
	{
		connectionServiceTracker.close();

		EntityMediator.removeListener(Domain.class, this);
		EntityMediator.removeListener(AddressGroupCategory.class, this);
		EntityMediator.removeListener(AddressGroup.class, this);
		EntityMediator.removeListener(AddressGroupMember.class, this);
		EntityMediator.removeListener(LinkPersonAddress.class, this);
		EntityMediator.removeListener(Person.class, this);
		EntityMediator.removeListener(Address.class, this);

		this.getSite().getPage().removeSelectionListener("ch.eugster.events.person.view", this);

		super.dispose();
	}

	private AddressGroupMember getMember(AddressGroup addressGroup, Address address)
	{
		List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (member.isValidAddressMember() && member.getAddress().getId().equals(address.getId()))
			{
				return member;
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

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		EntityMediator.addListener(Domain.class, this);
		EntityMediator.addListener(AddressGroupCategory.class, this);
		EntityMediator.addListener(AddressGroup.class, this);
		EntityMediator.addListener(AddressGroupMember.class, this);
		EntityMediator.addListener(LinkPersonAddress.class, this);
		EntityMediator.addListener(Address.class, this);
		EntityMediator.addListener(Person.class, this);

		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		connectionServiceTracker.open();

		this.setPartName("Adressgruppen");
	}
	
	private void internalRefresh()
	{
		if (!addressGroupViewer.getControl().isDisposed())
		{
			addressGroupViewer.refresh();
			checkParents();
		}
	}

	private void internalRefresh(Object object)
	{
		if (!addressGroupViewer.getControl().isDisposed())
		{
			addressGroupViewer.refresh(object);
			checkParents();
		}
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
		this.addressGroupViewer.getTree().setEnabled(false);
		this.monitors.clear();
		this.current.clear();

		List<AddressGroupMember> members = null;

		ConnectionService service = (ConnectionService) connectionServiceTracker.getService();
		if (service != null)
		{
			if (this.parent instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.parent;
				AddressGroupMemberQuery query = (AddressGroupMemberQuery) service.getQuery(AddressGroupMember.class);
				members = query.selectByLink(link);
			}
			else if (this.parent instanceof Address)
			{
				Address address = (Address) this.parent;
				AddressGroupMemberQuery query = (AddressGroupMemberQuery) service.getQuery(AddressGroupMember.class);
				members = query.selectByAddress(address);
			}
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
		this.checkParents();
		this.setDirty(false);
		this.addressGroupViewer.refresh();
		this.addressGroupViewer.getTree().setEnabled(this.parent != null);
	}
	
	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
	{
		if (this.dirty)
		{
			if (MessageDialog.openQuestion(this.getSite().getShell(), "Änderungen speichern", "Sie haben Änderungen an den aktuellen Adressgruppen vorgenommen. Sollen diese Änderungen gespeichert werden?"))
			{
				this.updateAddressGroupMembers();
			}
		}
		if (selection instanceof StructuredSelection)
		{
			IStructuredSelection ssel = (StructuredSelection) selection;
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
		}
		reset();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (event.getSource().equals(this.addressGroupViewer))
		{
			StructuredSelection ssel = (StructuredSelection) addressGroupViewer.getSelection();
			if (ssel.getFirstElement() instanceof Domain)
			{
				this.checkParents();
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
		this.addressGroupViewer.getTree().setFocus();
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
							AddressGroupMemberQuery query = (AddressGroupMemberQuery) service
									.getQuery(AddressGroupMember.class);
							member = query.merge(member);
							this.current.put(addressGroupId, member);
							service.refresh(this.parent);
							monitor.addressGroupMember = member;
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
						this.current.put(addressGroupId, query.merge(member));
						service.refresh(this.parent);
					}
				}
			}
		}
		this.setDirty(false);
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

}