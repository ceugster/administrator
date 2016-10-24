package ch.eugster.events.addressgroup.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.AddressGroupMemberParentType;
import ch.eugster.events.addressgroup.AddressGroupMemberSelector;
import ch.eugster.events.addressgroup.Monitor;
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
		ITreeViewerListener, ICheckStateListener, ISelectionListener, AddressGroupMemberSelector
{
	public static final String ID = "ch.eugster.events.addressgroup.person.view";

	private ContainerCheckedTreeViewer addressGroupViewer;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private final Map<Long, AddressGroupMember> current = new HashMap<Long, AddressGroupMember>();

	private boolean dirty;

	private ComboViewer domainViewer;

	private final Map<Long, Monitor> monitors = new HashMap<Long, Monitor>();

	private AbstractEntity parent;
	
	private IDialogSettings settings;

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
		this.addressGroupViewer.setContentProvider(new PersonAddressGroupMemberTreeContentProvider());
		this.addressGroupViewer.setLabelProvider(new PersonAddressGroupMemberTreeLabelProvider(monitors));
		this.addressGroupViewer.setSorter(new AddressGroupMemberTreeSorter(this));
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

		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand("ch.eugster.events.addressgroup.command.set.addressgroup.selection.link");
		State state = command.getState("ch.eugster.events.addressgroup.person.view.selection.link.state");
		state.setValue(settings.getInt("current.mode") == AddressGroupMemberParentType.LINK_PERSON_ADDRESS.ordinal());
		command = service.getCommand("ch.eugster.events.addressgroup.command.set.addressgroup.selection.address");
		state = command.getState("ch.eugster.events.addressgroup.person.view.selection.address.state");
		state.setValue(settings.getInt("current.mode") == AddressGroupMemberParentType.ADDRESS.ordinal());
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

	private AddressGroupMember getMember(AddressGroup addressGroup, Address address)
	{
		int mode = settings.getInt("current.mode");
		List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (mode == AddressGroupMemberParentType.ADDRESS.ordinal() && member.getAddress() != null && member.getAddress().getId().equals(address.getId()))
			{
				return member;
			}
		}
		return null;
	}

	private AddressGroupMember getMember(AddressGroup addressGroup, LinkPersonAddress link)
	{
		int mode = settings.getInt("current.mode");
		List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (mode == AddressGroupMemberParentType.LINK_PERSON_ADDRESS.ordinal() && member.getLink() != null && member.getLink().getId().equals(link.getId()))
			{
				return member;
			}
			else if (mode == AddressGroupMemberParentType.ADDRESS.ordinal() && member.getAddress() != null && member.getAddress().getId().equals(link.getAddress().getId()))
			{
				return member;
			}
		}
		return null;
	}

	public AddressGroupMemberParentType getMode()
	{
		return AddressGroupMemberParentType.values()[this.settings.getInt("current.mode")];
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

		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		connectionServiceTracker.open();

		this.setPartName("Adressgruppen");
		
		settings = Activator.getDefault().getDialogSettings().getSection("person.addressgroup.view");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("person.addressgroup.view");
		}
		try
		{
			settings.getInt("current.mode");
		}
		catch (NumberFormatException e)
		{
			settings.put("current.mode", AddressGroupMemberParentType.LINK_PERSON_ADDRESS.ordinal());
		}
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
		ConnectionService service = (ConnectionService) connectionServiceTracker.getService();
		if (service != null)
		{
			List<Domain> domains = null;
			if (domainViewer.getInput() == null)
			{
				DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
				domains = query.selectValids();
				if (domains.isEmpty())
				{
					domains.add(Domain.newInstance());
				}
				domainViewer.setInput(domains.toArray(new Domain[0]));
			}

			IStructuredSelection ssel = (IStructuredSelection) domainViewer.getSelection();
			if (ssel.isEmpty())
			{
				Domain selectedDomain = domains.get(0);
				if (this.parent instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) this.parent;
					if (link.getPerson().getDomain() != null)
					{
						selectedDomain = link.getPerson().getDomain();
					}
				}
				else
				{
					if (User.getCurrent() != null)
					{
						if (User.getCurrent().getDomain() != null)
						{
							selectedDomain = User.getCurrent().getDomain();
						}
					}
				}
				ssel = new StructuredSelection(selectedDomain);
				this.domainViewer.setSelection(ssel);
			}

			this.monitors.clear();
			this.current.clear();

			List<AddressGroupMember> members = null;

			if (this.parent instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.parent;
				AddressGroupMemberQuery query = (AddressGroupMemberQuery) service.getQuery(AddressGroupMember.class);
				if (settings.getInt("current.mode") == AddressGroupMemberParentType.LINK_PERSON_ADDRESS.ordinal())
				{
					members = query.selectByLink(link);
				}
				else if (settings.getInt("current.mode") == AddressGroupMemberParentType.ADDRESS.ordinal())
				{
					members = query.selectByAddress(link.getAddress());
				}
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
			this.checkCategory(ssel.getFirstElement());
		}
		this.setDirty(false);
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
			reset();
			ssel = (IStructuredSelection) domainViewer.getSelection();
			this.updateViewer(ssel.getFirstElement(), settings.getInt("current.mode"));
			this.checkCategory(ssel.getFirstElement());
		}
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (event.getSource().equals(this.domainViewer))
		{
			ComboViewer comboViewer = (ComboViewer) event.getSource();
			StructuredSelection ssel = (StructuredSelection) comboViewer.getSelection();
			this.updateViewer(ssel.getFirstElement(), settings.getInt("current.mode"));
			this.checkCategory(ssel.getFirstElement());
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

	public void setMode(AddressGroupMemberParentType mode)
	{
		int oldMode = this.settings.getInt("current.mode");
		this.settings.put("current.mode", mode.ordinal());
		if (oldMode != mode.ordinal())
		{
			if (this.dirty)
			{
				if (MessageDialog.openQuestion(this.getSite().getShell(), "Änderungen speichern", "Sie haben Änderungen an den aktuellen Adressgruppen vorgenommen. Sollen diese Änderungen gespeichert werden?"))
				{
					this.updateAddressGroupMembers();
				}
			}
			this.reset();
			this.refreshViewer();
		}
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
							if (this.settings.getInt("current.mode") == AddressGroupMemberParentType.ADDRESS.ordinal())
							{
								member = getMember(monitor.addressGroup, ((LinkPersonAddress) this.parent).getAddress());
								if (member == null)
								{
									member = AddressGroupMember.newInstance(monitor.addressGroup,
											((LinkPersonAddress) this.parent).getAddress());
								}
							}
							else if (this.settings.getInt("current.mode") == AddressGroupMemberParentType.LINK_PERSON_ADDRESS.ordinal())
							{
								member = getMember(monitor.addressGroup, (LinkPersonAddress) parent);
								if (member == null)
								{
									member = AddressGroupMember.newInstance(monitor.addressGroup,
											(LinkPersonAddress) this.parent);
								}
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

	private void updateViewer(final Object object, int currentMode)
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
	
}